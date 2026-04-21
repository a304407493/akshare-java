<template>
  <div class="market-overview-enhanced">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <h2>市场概况</h2>
      <p class="page-desc">展示上交所、深交所、北交所市场总貌及关键指标</p>
    </div>

    <!-- 关键指标卡片区域 -->
    <el-row :gutter="20" class="summary-cards">
      <!-- 上交所关键指标 -->
      <el-col :span="8">
        <el-card class="exchange-card sse-card" shadow="hover">
          <div slot="header" class="card-header">
            <span class="exchange-title">
              <i class="el-icon-office-building"></i>
              上海证券交易所
            </span>
            <el-tag type="danger" size="small">SSE</el-tag>
          </div>
          <div class="key-metrics" v-loading="sseLoading">
            <div class="metric-item">
              <div class="metric-label">上市公司数量</div>
              <div class="metric-value">{{ sseMetrics.companyCount }}</div>
            </div>
            <div class="metric-item">
              <div class="metric-label">总市值(亿元)</div>
              <div class="metric-value">{{ sseMetrics.totalMarketCap }}</div>
            </div>
            <div class="metric-item">
              <div class="metric-label">平均市盈率</div>
              <div class="metric-value">{{ sseMetrics.avgPe }}</div>
            </div>
          </div>
          <div class="card-footer">
            <el-button type="text" @click="loadSseData">
              <i class="el-icon-refresh"></i> 刷新数据
            </el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 深交所关键指标 -->
      <el-col :span="8">
        <el-card class="exchange-card szse-card" shadow="hover">
          <div slot="header" class="card-header">
            <span class="exchange-title">
              <i class="el-icon-office-building"></i>
              深圳证券交易所
            </span>
            <el-tag type="success" size="small">SZSE</el-tag>
          </div>
          <div class="key-metrics" v-loading="szseLoading">
            <div class="metric-item">
              <div class="metric-label">上市公司数量</div>
              <div class="metric-value">{{ szseMetrics.companyCount }}</div>
            </div>
            <div class="metric-item">
              <div class="metric-label">总市值(亿元)</div>
              <div class="metric-value">{{ szseMetrics.totalMarketCap }}</div>
            </div>
            <div class="metric-item">
              <div class="metric-label">平均市盈率</div>
              <div class="metric-value">{{ szseMetrics.avgPe }}</div>
            </div>
          </div>
          <div class="card-footer">
            <el-button type="text" @click="loadSzseData">
              <i class="el-icon-refresh"></i> 刷新数据
            </el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 北交所关键指标 -->
      <el-col :span="8">
        <el-card class="exchange-card bse-card" shadow="hover">
          <div slot="header" class="card-header">
            <span class="exchange-title">
              <i class="el-icon-office-building"></i>
              北京证券交易所
            </span>
            <el-tag type="warning" size="small">BSE</el-tag>
          </div>
          <div class="key-metrics" v-loading="bseLoading">
            <div class="metric-item">
              <div class="metric-label">上市公司数量</div>
              <div class="metric-value">{{ bseMetrics.companyCount }}</div>
            </div>
            <div class="metric-item">
              <div class="metric-label">总市值(亿元)</div>
              <div class="metric-value">{{ bseMetrics.totalMarketCap }}</div>
            </div>
            <div class="metric-item">
              <div class="metric-label">平均市盈率</div>
              <div class="metric-value">{{ bseMetrics.avgPe }}</div>
            </div>
          </div>
          <div class="card-footer">
            <el-button type="text" @click="loadBseData">
              <i class="el-icon-refresh"></i> 刷新数据
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 证券类别统计表格区域 -->
    <el-row :gutter="20" class="detail-tables">
      <!-- 上交所证券类别统计 -->
      <el-col :span="12">
        <el-card class="detail-card">
          <div slot="header" class="card-header">
            <span>上交所证券类别统计</span>
            <el-button type="primary" size="mini" @click="loadSseData">
              <i class="el-icon-refresh"></i> 刷新
            </el-button>
          </div>
          <el-table
            :data="sseData"
            style="width: 100%"
            v-loading="sseLoading"
            stripe
            border
            size="small"
          >
            <el-table-column prop="类别" label="类别" min-width="120" show-overflow-tooltip></el-table-column>
            <el-table-column prop="上市公司数量" label="上市公司数量" min-width="100" align="right"></el-table-column>
            <el-table-column prop="总市值(亿元)" label="总市值(亿元)" min-width="120" align="right">
              <template slot-scope="scope">
                <span class="number-cell">{{ formatNumber(scope.row['总市值(亿元)']) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="流通市值(亿元)" label="流通市值(亿元)" min-width="120" align="right">
              <template slot-scope="scope">
                <span class="number-cell">{{ formatNumber(scope.row['流通市值(亿元)']) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="平均市盈率" label="平均市盈率" min-width="100" align="right"></el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 深交所证券类别统计 -->
      <el-col :span="12">
        <el-card class="detail-card">
          <div slot="header" class="card-header">
            <span>深交所证券类别统计</span>
            <el-button type="primary" size="mini" @click="loadSzseData">
              <i class="el-icon-refresh"></i> 刷新
            </el-button>
          </div>
          <el-table
            :data="szseData"
            style="width: 100%"
            v-loading="szseLoading"
            stripe
            border
            size="small"
          >
            <el-table-column prop="类别" label="类别" min-width="120" show-overflow-tooltip></el-table-column>
            <el-table-column prop="上市公司数量" label="上市公司数量" min-width="100" align="right"></el-table-column>
            <el-table-column prop="总市值(亿元)" label="总市值(亿元)" min-width="120" align="right">
              <template slot-scope="scope">
                <span class="number-cell">{{ formatNumber(scope.row['总市值(亿元)']) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="流通市值(亿元)" label="流通市值(亿元)" min-width="120" align="right">
              <template slot-scope="scope">
                <span class="number-cell">{{ formatNumber(scope.row['流通市值(亿元)']) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="平均市盈率" label="平均市盈率" min-width="100" align="right"></el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 市场对比分析区域 -->
    <el-card class="comparison-card">
      <div slot="header" class="card-header">
        <span>市场对比分析</span>
        <el-button type="primary" size="mini" @click="loadAllData">
          <i class="el-icon-refresh"></i> 刷新全部
        </el-button>
      </div>
      <el-table
        :data="comparisonData"
        style="width: 100%"
        stripe
        border
      >
        <el-table-column prop="market" label="交易所" min-width="150">
          <template slot-scope="scope">
            <el-tag :type="scope.row.tagType" size="medium">{{ scope.row.market }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="companyCount" label="上市公司数量" min-width="120" align="right"></el-table-column>
        <el-table-column prop="totalMarketCap" label="总市值(亿元)" min-width="150" align="right">
          <template slot-scope="scope">
            <span class="number-cell">{{ formatNumber(scope.row.totalMarketCap) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="circulatingCap" label="流通市值(亿元)" min-width="150" align="right">
          <template slot-scope="scope">
            <span class="number-cell">{{ formatNumber(scope.row.circulatingCap) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="avgPe" label="平均市盈率" min-width="120" align="right"></el-table-column>
        <el-table-column prop="marketShare" label="市值占比" min-width="120" align="right">
          <template slot-scope="scope">
            <el-progress :percentage="scope.row.marketShare" :color="scope.row.progressColor"></el-progress>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { stockApi } from '../utils/api'

/**
 * 市场概况增强页面组件
 * 展示上交所、深交所、北交所的市场总貌及关键指标
 */
export default {
  name: 'MarketOverviewEnhanced',
  data() {
    return {
      // 上交所数据
      sseData: [],
      sseLoading: false,
      sseMetrics: {
        companyCount: '-',
        totalMarketCap: '-',
        avgPe: '-'
      },
      // 深交所数据
      szseData: [],
      szseLoading: false,
      szseMetrics: {
        companyCount: '-',
        totalMarketCap: '-',
        avgPe: '-'
      },
      // 北交所数据
      bseData: [],
      bseLoading: false,
      bseMetrics: {
        companyCount: '-',
        totalMarketCap: '-',
        avgPe: '-'
      }
    }
  },
  computed: {
    /**
     * 计算市场对比数据
     * @returns {Array} 对比数据数组
     */
    comparisonData() {
      const data = []
      
      // 计算总市值用于占比计算
      let totalCap = 0
      const sseCap = this.parseNumber(this.sseMetrics.totalMarketCap)
      const szseCap = this.parseNumber(this.szseMetrics.totalMarketCap)
      const bseCap = this.parseNumber(this.bseMetrics.totalMarketCap)
      totalCap = sseCap + szseCap + bseCap
      
      // 上交所数据
      if (this.sseData.length > 0) {
        data.push({
          market: '上海证券交易所',
          tagType: 'danger',
          companyCount: this.sseMetrics.companyCount,
          totalMarketCap: this.sseMetrics.totalMarketCap,
          circulatingCap: this.calculateCirculatingCap(this.sseData),
          avgPe: this.sseMetrics.avgPe,
          marketShare: totalCap > 0 ? Math.round((sseCap / totalCap) * 100) : 0,
          progressColor: '#F56C6C'
        })
      }
      
      // 深交所数据
      if (this.szseData.length > 0) {
        data.push({
          market: '深圳证券交易所',
          tagType: 'success',
          companyCount: this.szseMetrics.companyCount,
          totalMarketCap: this.szseMetrics.totalMarketCap,
          circulatingCap: this.calculateCirculatingCap(this.szseData),
          avgPe: this.szseMetrics.avgPe,
          marketShare: totalCap > 0 ? Math.round((szseCap / totalCap) * 100) : 0,
          progressColor: '#67C23A'
        })
      }
      
      // 北交所数据
      if (this.bseData.length > 0) {
        data.push({
          market: '北京证券交易所',
          tagType: 'warning',
          companyCount: this.bseMetrics.companyCount,
          totalMarketCap: this.bseMetrics.totalMarketCap,
          circulatingCap: this.calculateCirculatingCap(this.bseData),
          avgPe: this.bseMetrics.avgPe,
          marketShare: totalCap > 0 ? Math.round((bseCap / totalCap) * 100) : 0,
          progressColor: '#E6A23C'
        })
      }
      
      return data
    }
  },
  mounted() {
    // 页面加载时获取所有数据
    this.loadAllData()
  },
  methods: {
    /**
     * 加载所有市场数据
     */
    async loadAllData() {
      await Promise.all([
        this.loadSseData(),
        this.loadSzseData(),
        this.loadBseData()
      ])
    },
    
    /**
     * 加载上交所市场数据
     */
    async loadSseData() {
      this.sseLoading = true
      try {
        const response = await stockApi.getSseSummary()
        this.sseData = response.data || []
        this.calculateSseMetrics()
      } catch (error) {
        this.$message.error('加载上交所数据失败: ' + error.message)
      } finally {
        this.sseLoading = false
      }
    },
    
    /**
     * 加载深交所市场数据
     */
    async loadSzseData() {
      this.szseLoading = true
      try {
        const response = await stockApi.getSzseSummary()
        this.szseData = response.data || []
        this.calculateSzseMetrics()
      } catch (error) {
        this.$message.error('加载深交所数据失败: ' + error.message)
      } finally {
        this.szseLoading = false
      }
    },
    
    /**
     * 加载北交所市场数据
     */
    async loadBseData() {
      this.bseLoading = true
      try {
        // 北交所数据通过北京股票列表计算
        const response = await stockApi.getBjNameCode()
        this.bseData = response.data || []
        this.calculateBseMetrics()
      } catch (error) {
        this.$message.error('加载北交所数据失败: ' + error.message)
      } finally {
        this.bseLoading = false
      }
    },
    
    /**
     * 计算上交所关键指标
     */
    calculateSseMetrics() {
      if (this.sseData.length === 0) return
      
      // 计算总计行数据
      let totalCompanies = 0
      let totalCap = 0
      let weightedPe = 0
      
      this.sseData.forEach(item => {
        const companies = parseInt(item['上市公司数量']) || 0
        const cap = parseFloat(item['总市值(亿元)']) || 0
        const pe = parseFloat(item['平均市盈率']) || 0
        
        totalCompanies += companies
        totalCap += cap
        weightedPe += pe * companies
      })
      
      this.sseMetrics = {
        companyCount: totalCompanies.toLocaleString(),
        totalMarketCap: totalCap.toLocaleString(undefined, { maximumFractionDigits: 2 }),
        avgPe: totalCompanies > 0 ? (weightedPe / totalCompanies).toFixed(2) : '-'
      }
    },
    
    /**
     * 计算深交所关键指标
     */
    calculateSzseMetrics() {
      if (this.szseData.length === 0) return
      
      let totalCompanies = 0
      let totalCap = 0
      let weightedPe = 0
      
      this.szseData.forEach(item => {
        const companies = parseInt(item['上市公司数量']) || 0
        const cap = parseFloat(item['总市值(亿元)']) || 0
        const pe = parseFloat(item['平均市盈率']) || 0
        
        totalCompanies += companies
        totalCap += cap
        weightedPe += pe * companies
      })
      
      this.szseMetrics = {
        companyCount: totalCompanies.toLocaleString(),
        totalMarketCap: totalCap.toLocaleString(undefined, { maximumFractionDigits: 2 }),
        avgPe: totalCompanies > 0 ? (weightedPe / totalCompanies).toFixed(2) : '-'
      }
    },
    
    /**
     * 计算北交所关键指标
     */
    calculateBseMetrics() {
      if (this.bseData.length === 0) return
      
      this.bseMetrics = {
        companyCount: this.bseData.length.toLocaleString(),
        totalMarketCap: '-',
        avgPe: '-'
      }
    },
    
    /**
     * 计算流通市值总和
     * @param {Array} data - 市场数据数组
     * @returns {string} 格式化后的流通市值
     */
    calculateCirculatingCap(data) {
      let total = 0
      data.forEach(item => {
        total += parseFloat(item['流通市值(亿元)']) || 0
      })
      return total.toLocaleString(undefined, { maximumFractionDigits: 2 })
    },
    
    /**
     * 格式化数字显示
     * @param {string|number} value - 需要格式化的数值
     * @returns {string} 格式化后的字符串
     */
    formatNumber(value) {
      if (value === undefined || value === null || value === '-') return '-'
      const num = parseFloat(value)
      if (isNaN(num)) return value
      return num.toLocaleString(undefined, { maximumFractionDigits: 2 })
    },
    
    /**
     * 解析数值
     * @param {string|number} value - 需要解析的数值
     * @returns {number} 解析后的数值
     */
    parseNumber(value) {
      if (value === undefined || value === null || value === '-') return 0
      const num = parseFloat(value.toString().replace(/,/g, ''))
      return isNaN(num) ? 0 : num
    }
  }
}
</script>

<style scoped>
@import '../styles/MarketOverviewEnhanced.css';
</style>
