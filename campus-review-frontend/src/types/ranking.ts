/**
 * 热门餐馆榜单条目
 */
export interface HotRestaurantRankItemDTO {
  rank: number
  restaurantId: number
  restaurantName: string
  score: number
  avgRating: number
}
