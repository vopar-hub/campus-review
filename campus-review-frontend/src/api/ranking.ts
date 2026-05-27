import { get } from './request'
import type { HotRestaurantRankItemDTO } from '@/types'

/**
 * 获取热门餐馆排行榜
 */
export function getHotRestaurants(topN: number = 10) {
  return get<HotRestaurantRankItemDTO[]>('/api/rankings/hot-restaurants', {
    params: { topN },
  })
}
