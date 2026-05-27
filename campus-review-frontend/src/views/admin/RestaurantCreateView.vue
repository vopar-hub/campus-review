<template>
  <AppLayout>
    <div class="create-restaurant-page">
      <el-card class="form-card">
        <template #header>
          <div class="card-header">
            <el-icon :size="24"><Plus /></el-icon>
            <h2>添加餐厅</h2>
          </div>
        </template>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          label-position="top"
        >
          <el-form-item label="餐厅名称" prop="name">
            <el-input
              v-model="form.name"
              placeholder="请输入餐厅名称"
              clearable
              size="large"
            >
              <template #prefix>
                <el-icon><Shop /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="校区" prop="campus">
            <el-input
              v-model="form.campus"
              placeholder="例如：南湖校区、工学部校区"
              clearable
              size="large"
            >
              <template #prefix>
                <el-icon><Location /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="详细地址" prop="address">
            <el-input
              v-model="form.address"
              placeholder="请输入餐厅具体地址"
              clearable
              size="large"
            >
              <template #prefix>
                <el-icon><Position /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="餐厅描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="4"
              placeholder="请描述餐厅特色、菜品等"
              size="large"
            />
          </el-form-item>

          <el-form-item label="封面图片" prop="coverImageUrl">
            <div class="upload-section">
              <el-upload
                ref="uploadRef"
                class="upload-component"
                drag
                :auto-upload="false"
                :on-change="handleFileChange"
                :on-remove="handleFileRemove"
                :limit="1"
                accept="image/*"
              >
                <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
                <div class="el-upload__text">
                  将图片拖到此处，或<em>点击选择</em>
                </div>
                <template #tip>
                  <div class="el-upload__tip">
                    支持 jpg/png 格式，图片大小不超过 5MB
                  </div>
                </template>
              </el-upload>
              <div v-if="imagePreview" class="image-preview">
                <img :src="imagePreview" alt="封面预览" />
                <div class="image-actions">
                  <el-button type="danger" size="small" @click="removeImage">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </el-form-item>

          <el-form-item>
            <div class="form-actions">
              <el-button size="large" @click="handleReset">
                重置
              </el-button>
              <el-button
                type="primary"
                size="large"
                :loading="submitting"
                @click="handleSubmit"
              >
                创建餐厅
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules, UploadInstance, UploadUserFile } from 'element-plus'
import { Plus, Shop, Location, Position, UploadFilled, Delete } from '@element-plus/icons-vue'
import AppLayout from '@/components/layout/AppLayout.vue'
import { createRestaurant } from '@/api/restaurant'
import { uploadFile } from '@/api/file'
import type { RestaurantCreateRequest } from '@/types'
import { getLogger } from '@/utils/logger'

const logger = getLogger('views-admin-restaurant-create')

const router = useRouter()
const formRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()
const submitting = ref(false)
const imagePreview = ref<string>('')
const selectedFile = ref<File | null>(null)

const form = reactive<RestaurantCreateRequest>({
  name: '',
  campus: '',
  address: '',
  description: '',
  coverImageUrl: '',
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入餐厅名称', trigger: 'blur' },
    { min: 2, max: 50, message: '餐厅名称长度在 2-50 个字符之间', trigger: 'blur' },
  ],
  campus: [
    { required: true, message: '请输入校区', trigger: 'blur' },
  ],
  address: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { min: 5, message: '地址长度至少 5 个字符', trigger: 'blur' },
  ],
  description: [
    { required: true, message: '请输入餐厅描述', trigger: 'blur' },
    { min: 10, message: '描述长度至少 10 个字符', trigger: 'blur' },
  ],
}

const handleFileChange = (file: UploadUserFile) => {
  if (!file.raw) return

  // 检查文件大小 (5MB)
  const maxSize = 5 * 1024 * 1024
  if (file.raw.size > maxSize) {
    ElMessage.error('图片大小不能超过 5MB')
    uploadRef.value?.clearFiles()
    return
  }

  // 检查文件类型
  const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!validTypes.includes(file.raw.type)) {
    ElMessage.error('只支持 JPG/PNG/GIF/WEBP 格式的图片')
    uploadRef.value?.clearFiles()
    return
  }

  selectedFile.value = file.raw
  imagePreview.value = URL.createObjectURL(file.raw)
}

const handleFileRemove = () => {
  selectedFile.value = null
  imagePreview.value = ''
}

const removeImage = () => {
  uploadRef.value?.clearFiles()
  handleFileRemove()
}

const handleReset = () => {
  if (!formRef.value) return
  formRef.value.resetFields()
  uploadRef.value?.clearFiles()
  selectedFile.value = null
  imagePreview.value = ''
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    // 如果有选择图片，先上传图片
    if (selectedFile.value) {
      try {
        const uploadRes = await uploadFile(selectedFile.value, 'restaurant-covers')
        form.coverImageUrl = uploadRes.data.url
      } catch (error) {
        logger.error('图片上传失败:', error)
        ElMessage.error('图片上传失败，请重试')
        return
      }
    }

    submitting.value = true
    try {
      const submitData: RestaurantCreateRequest = {
        name: form.name,
        campus: form.campus,
        address: form.address,
        description: form.description,
        coverImageUrl: form.coverImageUrl || undefined,
      }

      await createRestaurant(submitData)
      ElMessage.success('餐厅创建成功')

      // 跳转到餐厅列表
      router.push('/restaurants')
    } catch (error) {
      logger.error('创建餐厅失败:', error)
    } finally {
      submitting.value = false
    }
  })
}
</script>

<style scoped>
.create-restaurant-page {
  max-width: 800px;
  margin: 0 auto;
  padding-bottom: 40px;
}

.form-card {
  border-radius: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #667eea;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #333;
  font-size: 15px;
}

:deep(.el-input__inner) {
  border-radius: 12px;
  border: 2px solid #e8eaf6;
}

:deep(.el-input__inner:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

:deep(.el-textarea__inner) {
  border-radius: 12px;
  border: 2px solid #e8eaf6;
}

:deep(.el-textarea__inner:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.upload-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.upload-component {
  width: 100%;
}

.upload-component :deep(.el-upload-dragger) {
  border-radius: 12px;
  border: 2px dashed #e8eaf6;
  padding: 32px;
  transition: all 0.3s ease;
}

.upload-component :deep(.el-upload-dragger:hover) {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.05);
}

.upload-component :deep(.el-icon--upload) {
  font-size: 48px;
  color: #667eea;
  margin-bottom: 16px;
}

.upload-component :deep(.el-upload__text) {
  color: #666;
  font-size: 14px;
}

.upload-component :deep(.el-upload__text em) {
  color: #667eea;
  font-style: normal;
  font-weight: 600;
}

.upload-component :deep(.el-upload__tip) {
  color: #999;
  font-size: 12px;
  margin-top: 8px;
}

.image-preview {
  position: relative;
  max-width: 400px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.image-preview img {
  width: 100%;
  height: auto;
  display: block;
}

.image-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  gap: 8px;
}

.image-actions .el-button {
  width: 32px;
  height: 32px;
  padding: 0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  width: 100%;
  margin-top: 24px;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  padding: 12px 32px;
  font-weight: 600;
}

:deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
}
</style>
