import Vue from 'vue'
import VueRouter from 'vue-router'
import StockRealtime from '../views/StockRealtime.vue'
import StockHistory from '../views/StockHistory.vue'
import StockList from '../views/StockList.vue'
import MarketOverview from '../views/MarketOverview.vue'
import MarketOverviewEnhanced from '../views/MarketOverviewEnhanced.vue'
import StockListEnhanced from '../views/StockListEnhanced.vue'
import StockDelist from '../views/StockDelist.vue'
import StockHolder from '../views/StockHolder.vue'
import StockHolderTrend from '../views/StockHolderTrend.vue'
import StockFinancial from '../views/StockFinancial.vue'
import StockFinancialIndicators from '../views/StockFinancialIndicators.vue'
import StockInstitution from '../views/StockInstitution.vue'
import StockInstitutionDetail from '../views/StockInstitutionDetail.vue'
import StockInstitutionRank from '../views/StockInstitutionRank.vue'
import StockAuction from '../views/StockAuction.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    redirect: '/stock/realtime'
  },
  {
    path: '/stock/realtime',
    name: 'StockRealtime',
    component: StockRealtime
  },
  {
    path: '/stock/history',
    name: 'StockHistory',
    component: StockHistory
  },
  {
    path: '/stock/list',
    name: 'StockList',
    component: StockList
  },
  {
    path: '/stock/market',
    name: 'MarketOverview',
    component: MarketOverview
  },
  {
    path: '/stock/market-enhanced',
    name: 'MarketOverviewEnhanced',
    component: MarketOverviewEnhanced
  },
  {
    path: '/stock/list-enhanced',
    name: 'StockListEnhanced',
    component: StockListEnhanced
  },
  {
    path: '/stock/delist',
    name: 'StockDelist',
    component: StockDelist
  },
  {
    path: '/stock/holder',
    name: 'StockHolder',
    component: StockHolder
  },
  {
    path: '/stock/holder-trend',
    name: 'StockHolderTrend',
    component: StockHolderTrend
  },
  {
    path: '/stock/financial',
    name: 'StockFinancial',
    component: StockFinancial
  },
  {
    path: '/stock/financial-indicators',
    name: 'StockFinancialIndicators',
    component: StockFinancialIndicators
  },
  {
    path: '/stock/institution',
    name: 'StockInstitution',
    component: StockInstitution
  },
  {
    path: '/stock/institution/detail',
    name: 'StockInstitutionDetail',
    component: StockInstitutionDetail
  },
  {
    path: '/stock/institution/rank',
    name: 'StockInstitutionRank',
    component: StockInstitutionRank
  },
  {
    path: '/stock/auction',
    name: 'StockAuction',
    component: StockAuction
  }
]

const router = new VueRouter({
  mode: 'history',
  routes
})

export default router
