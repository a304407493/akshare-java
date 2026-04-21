<template>
  <!-- 机构排行页面容器 -->
  <div class="stock-institution-rank">
    <!-- 主卡片组件 -->
    <el-card>
      <!-- 卡片头部：标题和刷新按钮 -->
      <div slot="header" class="clearfix">
        <span>机构排行</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="loadData">
          <i class="el-icon-refresh"></i> 刷新
        </el-button>
      </div>

      <!-- 标签页切换：机构买卖排行和营业部排行 -->
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <!-- 机构买卖排行标签页 -->
        <el-tab-pane label="机构买卖排行" name="jgmm">
          <!-- 查询条件区域 -->
          <div class="search-area">
            <!-- 开始日期选择器 -->
            <el-date-picker
              v-model="jgmmStartDate"
              type="date"
              placeholder="开始日期"
              value-format="yyyyMMdd"
              style="margin-right: 10px;"
            ></el-date-picker>
            <!-- 结束日期选择器 -->
            <el-date-picker
              v-model="jgmmEndDate"
              type="date"
              placeholder="结束日期"
              value-format="yyyyMMdd"
              style="margin-right: 10px;"
            ></el-date-picker>
            <!-- 查询按钮 -->
            <el-button type="primary" @click="handleJgmmSearch">
              <i class="el-icon-search"></i> 查询
            </el-button>
          </div>

          <!-- 机构买卖排行表格 -->
          <el-table
            :data="jgmmData"
            stripe
            style="width: 100%;"
            height="500"
            v-loading="jgmmLoading"
            @sort-change="handleJgmmSort"
          >
            <!-- 序号列 -->
            <el-table-column prop="序号" label="序号" width="60"></el-table-column>
            <!-- 股票代码列 -->
            <el-table-column prop="代码" label="代码" width="100"></el-table-column>
            <!-- 股票名称列 -->
            <el-table-column prop="名称" label="名称" width="120"></el-table-column>
            <!-- 上榜日期列 -->
            <el-table-column prop="上榜日" label="上榜日期" width="120"></el-table-column>
            <!-- 收盘价列 -->
            <el-table-column prop="收盘价" label="收盘价" width="100">
              <template slot-scope="scope">
                <span>{{ formatNumber(scope.row['收盘价']) }}</span>
              </template>
            </el-table-column>
            <!-- 涨跌幅列 -->
            <el-table-column prop="涨跌幅" label="涨跌幅" width="100" sortable>
              <template slot-scope="scope">
                <span :class="getChangeClass(scope.row['涨跌幅'])">
                  {{ formatChange(scope.row['涨跌幅']) }}
                </span>
              </template>
            </el-table-column>
            <!-- 龙虎榜净买额列：支持排序 -->
            <el-table-column prop="龙虎榜净买额" label="净买额(万)" width="130" sortable>
              <template slot-scope="scope">
                <span :class="getAmountClass(scope.row['龙虎榜净买额'])">
                  {{ formatAmount(scope.row['龙虎榜净买额']) }}
                </span>
              </template>
            </el-table-column>
            <!-- 龙虎榜买入额列 -->
            <el-table-column prop="龙虎榜买入额" label="买入额(万)" width="130">
              <template slot-scope="scope">
                <span class="positive-amount">{{ formatAmount(scope.row['龙虎榜买入额']) }}</span>
              </template>
            </el-table-column>
            <!-- 龙虎榜卖出额列 -->
            <el-table-column prop="龙虎榜卖出额" label="卖出额(万)" width="130">
              <template slot-scope="scope">
                <span class="negative-amount">{{ formatAmount(scope.row['龙虎榜卖出额']) }}</span>
              </template>
            </el-table-column>
            <!-- 龙虎榜成交额列 -->
            <el-table-column prop="龙虎榜成交额" label="成交额(万)" width="130" sortable>
              <template slot-scope="scope">
                <span>{{ formatAmount(scope.row['龙虎榜成交额']) }}</span>
              </template>
            </el-table-column>
            <!-- 换手率列 -->
            <el-table-column prop="换手率" label="换手率" width="100">
              <template slot-scope="scope">
                <span>{{ formatPercent(scope.row['换手率']) }}</span>
              </template>
            </el-table-column>
            <!-- 上榜原因列 -->
            <el-table-column prop="上榜原因" label="上榜原因" min-width="200" show-overflow-tooltip></el-table-column>
            <!-- 操作列 -->
            <el-table-column label="操作" width="120" fixed="right">
              <template slot-scope="scope">
                <el-button
                  size="mini"
                  type="primary"
                  @click="viewDetail(scope.row)"
                >
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 营业部排行标签页 -->
        <el-tab-pane label="营业部排行" name="yybph">
          <!-- 查询条件区域 -->
          <div class="search-area">
            <!-- 时间周期选择器 -->
            <el-select v-model="yybphPeriod" placeholder="时间周期" style="margin-right: 10px; width: 150px;">
              <el-option label="近一月" value="近一月"></el-option>
              <el-option label="近三月" value="近三月"></el-option>
              <el-option label="近六月" value="近六月"></el-option>
              <el-option label="近一年" value="近一年"></el-option>
            </el-select>
            <!-- 查询按钮 -->
            <el-button type="primary" @click="handleYybphSearch">
              <i class="el-icon-search"></i> 查询
            </el-button>
          </div>

          <!-- 营业部排行表格 -->
          <el-table
            :data="yybphData"
            stripe
            style="width: 100%;"
            height="500"
            v-loading="yybphLoading"
            @sort-change="handleYybphSort"
          >
            <!-- 序号列 -->
            <el-table-column prop="序号" label="序号" width="60"></el-table-column>
            <!-- 营业部名称列 -->
            <el-table-column prop="营业部名称" label="营业部名称" min-width="250" show-overflow-tooltip></el-table-column>
            <!-- 上榜次数列：支持排序 -->
            <el-table-column prop="上榜次数" label="上榜次数" width="100" sortable>
              <template slot-scope="scope">
                <el-tag type="primary">{{ scope.row['上榜次数'] }}</el-tag>
              </template>
            </el-table-column>
            <!-- 买入个股数列 -->
            <el-table-column prop="买入个股数" label="买入个股数" width="110">
              <template slot-scope="scope">
                <span class="positive-amount">{{ scope.row['买入个股数'] }}</span>
              </template>
            </el-table-column>
            <!-- 卖出个股数列 -->
            <el-table-column prop="卖出个股数" label="卖出个股数" width="110">
              <template slot-scope="scope">
                <span class="negative-amount">{{ scope.row['卖出个股数'] }}</span>
              </template>
            </el-table-column>
            <!-- 买入总金额列：支持排序 -->
            <el-table-column prop="买入总金额" label="买入总金额(万)" width="140" sortable>
              <template slot-scope="scope">
                <span class="positive-amount">{{ formatAmount(scope.row['买入总金额']) }}</span>
              </template>
            </el-table-column>
            <!-- 卖出总金额列 -->
            <el-table-column prop="卖出总金额" label="卖出总金额(万)" width="140" sortable>
              <template slot-scope="scope">
                <span class="negative-amount">{{ formatAmount(scope.row['卖出总金额']) }}</span>
              </template>
            </el-table-column>
            <!-- 总买卖净额列：支持排序 -->
            <el-table-column prop="总买卖净额" label="总买卖净额(万)" width="140" sortable>
              <template slot-scope="scope">
                <span :class="getAmountClass(scope.row['总买卖净额'])">
                  {{ formatAmount(scope.row['总买卖净额']) }}
                </span>
              </template>
            </el-table-column>
            <!-- 买入股票列 -->
            <el-table-column prop="买入股票" label="买入股票" min-width="300" show-overflow-tooltip></el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>

      <!-- 分页组件 -->
      <div class="pagination-area">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        ></el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script>
// 导入机构数据API
import { institutionApi } from '../utils/api'

export default {
  name: 'StockInstitutionRank',
  data() {
    return {
      // 当前激活的标签页
      activeTab: 'jgmm',
      // 机构买卖排行数据
      jgmmData: [],
      // 机构买卖排行加载状态
      jgmmLoading: false,
      // 机构买卖排行开始日期
      jgmmStartDate: this.getDefaultDate(),
      // 机构买卖排行结束日期
      jgmmEndDate: this.getDefaultDate(),
      // 营业部排行数据
      yybphData: [],
      // 营业部排行加载状态
      yybphLoading: false,
      // 营业部排行时间周期
      yybphPeriod: '近一月',
      // 当前页码
      currentPage: 1,
      // 每页条数
      pageSize: 20,
      // 总记录数
      total: 0
    }
  },
  mounted() {
    // 组件挂载后加载数据
    this.loadData()
  },
  methods: {
    /**
     * 获取默认日期（今天）
     * @returns {string} 格式化的日期字符串 yyyyMMdd
     */
    getDefaultDate() {
      const date = new Date()
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}${month}${day}`
    },
    /**
     * 加载数据
     * 根据当前激活的标签页加载对应数据
     */
    async loadData() {
      if (this.activeTab === 'jgmm') {
        await this.loadJgmmData()
      } else {
        await this.loadYybphData()
      }
    },
    /**
     * 加载机构买卖排行数据
     * 调用后端API获取机构买卖统计数据
     */
    async loadJgmmData() {
      this.jgmmLoading = true
      try {
        const response = await institutionApi.getLhbJgmm(this.jgmmStartDate, this.jgmmEndDate)
        this.jgmmData = response.data || []
        this.total = this.jgmmData.length
        this.$message.success(`成功加载 ${this.total} 条机构买卖数据`)
      } catch (error) {
        this.$message.error('加载机构买卖数据失败: ' + error.message)
      } finally {
        this.jgmmLoading = false
      }
    },
    /**
     * 加载营业部排行数据
     * 调用后端API获取营业部排行数据
     */
    async loadYybphData() {
      this.yybphLoading = true
      try {
        const response = await institutionApi.getLhbYybph(this.yybphPeriod)
        this.yybphData = response.data || []
        this.total = this.yybphData.length
        this.$message.success(`成功加载 ${this.total} 条营业部排行数据`)
      } catch (error) {
        this.$message.error('加载营业部排行数据失败: ' + error.message)
      } finally {
        this.yybphLoading = false
      }
    },
    /**
     * 处理标签页切换
     * 重置页码并加载对应数据
     */
    handleTabChange() {
      this.currentPage = 1
      this.loadData()
    },
    /**
     * 处理机构买卖排行查询
     */
    handleJgmmSearch() {
      this.currentPage = 1
      this.loadJgmmData()
    },
    /**
     * 处理营业部排行查询
     */
    handleYybphSearch() {
      this.currentPage = 1
      this.loadYybphData()
    },
    /**
     * 查看个股详情
     * 跳转到龙虎榜详情页面
     * @param {Object} row 当前行数据
     */
    viewDetail(row) {
      this.$router.push({
        path: '/stock/institution/detail',
        query: {
          symbol: row['代码'],
          name: row['名称'],
          date: row['上榜日']
        }
      })
    },
    /**
     * 处理机构买卖排行表格排序
     * @param {Object} sort 排序信息
     */
    handleJgmmSort(sort) {
      if (!sort.prop || !sort.order) return
      const prop = sort.prop
      const order = sort.order === 'ascending' ? 1 : -1
      this.jgmmData.sort((a, b) => {
        const valA = Number(a[prop]) || 0
        const valB = Number(b[prop]) || 0
        return (valA - valB) * order
      })
    },
    /**
     * 处理营业部排行表格排序
     * @param {Object} sort 排序信息
     */
    handleYybphSort(sort) {
      if (!sort.prop || !sort.order) return
      const prop = sort.prop
      const order = sort.order === 'ascending' ? 1 : -1
      this.yybphData.sort((a, b) => {
        const valA = Number(a[prop]) || 0
        const valB = Number(b[prop]) || 0
        return (valA - valB) * order
      })
    },
    /**
     * 处理每页条数变化
     * @param {number} val 新的每页条数
     */
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
    },
    /**
     * 处理页码变化
     * @param {number} val 新的页码
     */
    handleCurrentChange(val) {
      this.currentPage = val
    },
    /**
     * 格式化数字
     * @param {number} value 数值
     * @returns {string} 格式化后的字符串
     */
    formatNumber(value) {
      if (value === null || value === undefined) return '-'
      return Number(value).toFixed(2)
    },
    /**
     * 格式化涨跌幅，添加%符号
     * @param {number} value 涨跌幅数值
     * @returns {string} 格式化后的字符串
     */
    formatChange(value) {
      if (value === null || value === undefined) return '-'
      const num = Number(value)
      return (num > 0 ? '+' : '') + num.toFixed(2) + '%'
    },
    /**
     * 格式化金额，转换为万为单位
     * @param {number} value 金额数值
     * @returns {string} 格式化后的字符串
     */
    formatAmount(value) {
      if (value === null || value === undefined) return '-'
      return (Number(value) / 10000).toFixed(2)
    },
    /**
     * 格式化百分比
     * @param {number} value 百分比数值
     * @returns {string} 格式化后的字符串
     */
    formatPercent(value) {
      if (value === null || value === undefined) return '-'
      return Number(value).toFixed(2) + '%'
    },
    /**
     * 获取涨跌幅的样式类名
     * @param {number} value 涨跌幅数值
     * @returns {string} 样式类名
     */
    getChangeClass(value) {
      if (value === null || value === undefined) return ''
      const num = Number(value)
      if (num > 0) return 'positive-change'
      if (num < 0) return 'negative-change'
      return ''
    },
    /**
     * 获取金额的正负样式类名
     * @param {number} value 金额数值
     * @returns {string} 样式类名
     */
    getAmountClass(value) {
      if (value === null || value === undefined) return ''
      const num = Number(value)
      if (num > 0) return 'positive-amount'
      if (num < 0) return 'negative-amount'
      return ''
    }
  }
}
</script>

<style scoped>
/* 页面容器样式 */
.stock-institution-rank {
  padding: 0;
}

/* 头部清除浮动样式 */
.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both;
}

/* 查询区域样式 */
.search-area {
  margin-bottom: 20px;
}

/* 分页区域样式 */
.pagination-area {
  margin-top: 20px;
  text-align: right;
}

/* 上涨样式（红色） */
.positive-change {
  color: #f56c6c;
  font-weight: bold;
}

/* 下跌样式（绿色） */
.negative-change {
  color: #67c23a;
  font-weight: bold;
}

/* 正金额样式（红色） */
.positive-amount {
  color: #f56c6c;
}

/* 负金额样式（绿色） */
.negative-amount {
  color: #67c23a;
}
</style>
