import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8765/api',
  timeout: 30000
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      console.error('API Error:', res.message)
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  error => {
    console.error('Request Error:', error)
    return Promise.reject(error)
  }
)

// 股票相关API
export const stockApi = {
  // 获取实时行情（东方财富）
  getRealtimeEm: () => api.get('/stock/realtime/em'),

  // 获取实时行情（新浪）
  getRealtimeSina: () => api.get('/stock/realtime/sina'),

  // 获取历史K线
  getHistory: (params) => api.get('/stock/history', { params }),

  // 获取分钟K线
  getMinuteHistory: (params) => api.get('/stock/history/minute', { params }),

  // 获取股票列表
  getStockList: () => api.get('/stock/list'),

  // 获取上交所市场概况
  getSseSummary: () => api.get('/stock/market/sse-summary'),

  // 获取深交所市场概况
  getSzseSummary: () => api.get('/stock/market/szse-summary'),

  // 获取个股信息
  getStockInfo: (symbol) => api.get(`/stock/info/${symbol}`),

  // 获取沪市股票名录
  getShNameCode: () => api.get('/stock/market/sh-name-code'),

  // 获取深市股票名录
  getSzNameCode: () => api.get('/stock/market/sz-name-code'),

  // 获取北交所股票名录
  getBjNameCode: () => api.get('/stock/market/bj-name-code'),

  // 获取沪市退市股票
  getShDelist: () => api.get('/stock/market/sh-delist'),

  // 获取深市退市股票
  getSzDelist: () => api.get('/stock/market/sz-delist'),

  // 获取十大流通股东
  // symbol: 股票代码，date: 报告期日期，格式如"20240930"
  getTop10FreeHolders: (symbol, date) => api.get(`/stock/holder/top10-free?symbol=${symbol}&date=${date}`),

  // 获取十大股东
  // symbol: 股票代码，date: 报告期日期，格式如"20240930"
  getTop10Holders: (symbol, date) => api.get(`/stock/holder/top10?symbol=${symbol}&date=${date}`),

  // 获取持股详情
  // symbol: 股票代码，date: 报告期日期，格式如"20240930"
  getHoldingDetail: (symbol, date) => api.get(`/stock/holder/holding-detail?symbol=${symbol}&date=${date}`),

  // 获取流通持股详情
  // symbol: 股票代码，date: 报告期日期，格式如"20240930"
  getFreeHoldingDetail: (symbol, date) => api.get(`/stock/holder/free-holding-detail?symbol=${symbol}&date=${date}`),

  // 获取股东户数数据
  // symbol: 股票代码
  getHolderNum: (symbol) => api.get(`/stock/holder/holder-num?symbol=${symbol}`),

  // 获取集合竞价数据
  // symbol: 股票代码
  getAuctionData: (symbol) => api.get('/stock/auction', { params: { symbol } }),

  // 获取买卖五档数据
  // symbol: 股票代码
  getBidAskData: (symbol) => api.get('/stock/bid-ask', { params: { symbol } })
}

// 财务数据相关API
export const financialApi = {
  // 获取财务报表数据（通用接口）
  // symbol: 股票代码，reportType: 报表类型(1-按报告期,2-按年度,3-按单季度)，reportName: 报表名称
  getFinancialReport: (params) => api.get('/stock/financial/report', { params }),

  // 获取财务分析指标数据
  // symbol: 股票代码，reportType: 报表类型
  getFinancialAnalysis: (params) => api.get('/stock/financial/analysis', { params }),

  // 获取利润表数据
  // symbol: 股票代码，reportType: 报表类型
  getProfitSheet: (params) => api.get('/stock/financial/profit-sheet', { params }),

  // 获取资产负债表数据
  // symbol: 股票代码，reportType: 报表类型
  getBalanceSheet: (params) => api.get('/stock/financial/balance-sheet', { params }),

  // 现金流量表数据
  // symbol: 股票代码，reportType: 报表类型
  getCashFlowSheet: (params) => api.get('/stock/financial/cash-flow-sheet', { params })
}

/**
 * 机构数据相关API
 * 提供龙虎榜、机构买卖统计、营业部排行等机构相关数据
 */
export const institutionApi = {
  /**
   * 获取龙虎榜详情数据
   * @param {string} startDate 开始日期，格式：yyyyMMdd
   * @param {string} endDate 结束日期，格式：yyyyMMdd
   * @returns {Promise} 返回龙虎榜详情数据
   * 数据字段：序号、代码、名称、上榜日、解读、收盘价、涨跌幅、龙虎榜净买额、
   * 龙虎榜买入额、龙虎榜卖出额、龙虎榜成交额、市场总成交额、净买额占总成交比、
   * 成交额占总成交比、换手率、流通市值、上榜原因、上榜后1日、上榜后2日、上榜后5日、上榜后10日
   */
  getLhbDetail: (startDate, endDate) => api.get('/stock/institution/lhb-detail', {
    params: { startDate, endDate }
  }),

  /**
   * 获取个股龙虎榜详情数据
   * @param {string} symbol 股票代码，如：000001
   * @param {string} date 交易日期，格式：yyyyMMdd
   * @param {string} flag 买卖方向，可选值："买入"、"卖出"，不传则返回全部
   * @returns {Promise} 返回个股龙虎榜详情数据
   */
  getLhbStockDetail: (symbol, date, flag) => api.get('/stock/institution/lhb-stock-detail', {
    params: { symbol, date, flag }
  }),

  /**
   * 获取机构买卖统计数据
   * @param {string} startDate 开始日期，格式：yyyyMMdd
   * @param {string} endDate 结束日期，格式：yyyyMMdd
   * @returns {Promise} 返回机构买卖统计数据
   */
  getLhbJgmm: (startDate, endDate) => api.get('/stock/institution/lhb-jgmm', {
    params: { startDate, endDate }
  }),

  /**
   * 获取营业部排行数据
   * @param {string} period 时间周期，可选值："近一月"、"近三月"、"近六月"、"近一年"，默认"近一月"
   * @returns {Promise} 返回营业部排行数据
   * 数据字段：序号、营业部名称、上榜次数、买入个股数、卖出个股数、
   * 买入总金额、卖出总金额、总买卖净额、买入股票
   */
  getLhbYybph: (period) => api.get('/stock/institution/lhb-yybph', {
    params: { period }
  })
}

export default api
