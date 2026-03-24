package com.vapor.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vapor.common.error.BizException;
import com.vapor.common.error.ErrorCode;
import com.vapor.common.web.UserContext;
import com.vapor.common.web.UserContextHolder;
import com.vapor.model.auth.LoginRequest;
import com.vapor.model.auth.LoginResponse;
import com.vapor.model.auth.RegisterRequest;
import com.vapor.model.user.UserDTO;
import com.vapor.user.entity.UserEntity;
import com.vapor.user.mapper.UserMapper;
import com.vapor.user.service.UserAccountService;
import com.vapor.utils.jwt.JwtClaims;
import com.vapor.utils.jwt.JwtService;
import com.vapor.utils.validate.CampusAccountValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

/**
 * 用户账号服务实现类。
 */
@Service
public class UserAccountServiceImpl implements UserAccountService {
    private static final Logger log = LoggerFactory.getLogger(UserAccountServiceImpl.class);

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * 构造应用服务。
     *
     * @param userMapper 用户数据访问组件
     * @param passwordEncoder 密码编码器
     * @param jwtService JWT 服务
     */
    public UserAccountServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public UserDTO register(RegisterRequest request) {
        log.info("用户注册：email={}, studentNo={}, nickname={}", request.email(), request.studentNo(), request.nickname());

        if (!CampusAccountValidator.isValidEmail(request.email())) {
            log.warn("注册失败 - 邮箱格式不正确：email={}", request.email());
            throw new BizException(ErrorCode.BAD_REQUEST, "邮箱格式不正确");
        }
        if (!CampusAccountValidator.isValidStudentNo(request.studentNo())) {
            log.warn("注册失败 - 学号格式不正确：studentNo={}", request.studentNo());
            throw new BizException(ErrorCode.BAD_REQUEST, "学号格式不正确");
        }

        boolean emailExists = userMapper.exists(new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getEmail, request.email()));
        if (emailExists) {
            log.warn("注册失败 - 邮箱已注册：email={}", request.email());
            throw new BizException(ErrorCode.BAD_REQUEST, "邮箱已注册");
        }

        boolean studentExists = userMapper.exists(new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getStudentNo, request.studentNo()));
        if (studentExists) {
            log.warn("注册失败 - 学号已注册：studentNo={}", request.studentNo());
            throw new BizException(ErrorCode.BAD_REQUEST, "学号已注册");
        }

        Instant now = Instant.now();
        UserEntity entity = new UserEntity();
        entity.setEmail(request.email());
        entity.setStudentNo(request.studentNo());
        entity.setNickname(request.nickname());
        entity.setPasswordHash(passwordEncoder.encode(request.password()));
        entity.setRoles("USER");
        entity.setBanned(false);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        userMapper.insert(entity);

        log.info("注册成功：userId={}, email={}", entity.getId(), entity.getEmail());
        return toDTO(entity);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("用户登录：account={}", request.account());

        UserEntity user = findByAccount(request.account());
        if (user == null) {
            log.warn("登录失败 - 账号不存在：account={}", request.account());
            throw new BizException(ErrorCode.UNAUTHORIZED, "账号或密码错误");
        }

        if (Boolean.TRUE.equals(user.getBanned())) {
            log.warn("登录失败 - 账号已被封禁：userId={}, account={}", user.getId(), request.account());
            throw new BizException(ErrorCode.FORBIDDEN, "账号已被封禁");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("登录失败 - 密码错误：userId={}, account={}", user.getId(), request.account());
            throw new BizException(ErrorCode.UNAUTHORIZED, "账号或密码错误");
        }

        Set<String> roles = JwtClaims.parseRoles(user.getRoles());
        String token = jwtService.issue(user.getId(), roles);
        JwtClaims claims = jwtService.parseAndValidate(token);

        log.info("登录成功：userId={}, account={}", user.getId(), request.account());
        return new LoginResponse(user.getId(), roles, token, claims.expiresAt());
    }

    @Override
    public UserDTO me() {
        UserContext ctx = UserContextHolder.get();
        if (ctx == null || ctx.getUserId() == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }

        UserEntity entity = userMapper.selectById(ctx.getUserId());
        if (entity == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return toDTO(entity);
    }

    /**
     * 通过账号查找用户。
     *
     * @param account 账号（邮箱或学号）
     * @return 用户实体；账号为空时返回 null
     */
    private UserEntity findByAccount(String account) {
        if (account == null || account.isBlank()) {
            return null;
        }
        return userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getEmail, account)
                .or()
                .eq(UserEntity::getStudentNo, account)
                .last("limit 1"));
    }

    /**
     * 将用户实体映射为对外 DTO。
     *
     * @param entity 用户实体
     * @return 用户 DTO
     */
    private UserDTO toDTO(UserEntity entity) {
        return new UserDTO(
                entity.getId(),
                entity.getEmail(),
                entity.getStudentNo(),
                entity.getNickname(),
                JwtClaims.parseRoles(entity.getRoles()),
                Boolean.TRUE.equals(entity.getBanned()),
                entity.getCreatedAt()
        );
    }
}
