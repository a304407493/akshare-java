<template>
  <!-- 财务数据页面主容器 -->
  <div class="stock-financial">
    <el-card>
      <!-- 卡片头部：标题和刷新按钮 -->
      <div slot="header" class="clearfix">
        <span>财务数据</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="loadAllData">
          <i class="el-icon-refresh"></i> 刷新
        </el-button>
      </div>

      <!-- 查询条件表单 -->
      <el-form :inline="true" :model="queryForm" class="demo-form-inline">
        <!-- 股票代码输入框 -->
        <el-form-item label="股票代码">
          <el-input v-model="queryForm.symbol" placeholder="请输入股票代码" clearable></el-input>
        </el-form-item>
        <!-- 报表类型选择器 -->
        <el-form-item label="报表类型">
          <el-select v-model="queryForm.reportType" placeholder="请选择报表类型">
            <el-option label="按报告期" value="1"></el-option>
            <el-option label="按年度" value="2"></el-option>
            <el-option label="按单季度" value="3"></el-option>
          </el-select>
        </el-form-item>
        <!-- 查询按钮 -->
        <el-form-item>
          <el-button type="primary" @click="loadAllData">查询</el-button>
        </el-form-item>
      </el-form>

      <!-- 财务报表标签页 -->
      <el-tabs v-model="activeTab" type="border-card" @tab-click="handleTabChange">
        <!-- 资产负债表标签页 -->
        <el-tab-pane label="资产负债表" name="balance">
          <div class="table-title">资产负债表（单位：元）</div>
          <el-table
            :data="balanceSheetData"
            stripe
            style="width: 100%;"
            height="500"
            v-loading="loading.balance"
          >
            <!-- 报告期列 -->
            <el-table-column prop="报告期" label="报告期" width="120" fixed="left"></el-table-column>
            <!-- 动态生成资产负债表列 -->
            <el-table-column
              v-for="col in balanceSheetColumns"
              :key="col.prop"
              :prop="col.prop"
              :label="col.label"
              :width="col.width || 150"
              show-overflow-tooltip
            >
              <!-- 数值格式化显示 -->
              <template slot-scope="scope">
                {{ formatNumber(scope.row[col.prop]) }}
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 利润表标签页 -->
        <el-tab-pane label="利润表" name="profit">
          <div class="table-title">利润表（单位：元）</div>
          <el-table
            :data="profitSheetData"
            stripe
            style="width: 100%;"
            height="500"
            v-loading="loading.profit"
          >
            <!-- 报告期列 -->
            <el-table-column prop="报告期" label="报告期" width="120" fixed="left"></el-table-column>
            <!-- 动态生成利润表列 -->
            <el-table-column
              v-for="col in profitSheetColumns"
              :key="col.prop"
              :prop="col.prop"
              :label="col.label"
              :width="col.width || 150"
              show-overflow-tooltip
            >
              <!-- 数值格式化显示 -->
              <template slot-scope="scope">
                {{ formatNumber(scope.row[col.prop]) }}
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 现金流量表标签页 -->
        <el-tab-pane label="现金流量表" name="cashflow">
          <div class="table-title">现金流量表（单位：元）</div>
          <el-table
            :data="cashFlowSheetData"
            stripe
            style="width: 100%;"
            height="500"
            v-loading="loading.cashflow"
          >
            <!-- 报告期列 -->
            <el-table-column prop="报告期" label="报告期" width="120" fixed="left"></el-table-column>
            <!-- 动态生成现金流量表列 -->
            <el-table-column
              v-for="col in cashFlowSheetColumns"
              :key="col.prop"
              :prop="col.prop"
              :label="col.label"
              :width="col.width || 180"
              show-overflow-tooltip
            >
              <!-- 数值格式化显示 -->
              <template slot-scope="scope">
                {{ formatNumber(scope.row[col.prop]) }}
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
// 导入财务数据相关API
import { financialApi } from '../utils/api'

export default {
  name: 'StockFinancial',
  data() {
    return {
      // 查询表单数据
      queryForm: {
        symbol: '000001', // 默认股票代码
        reportType: '1'   // 默认按报告期
      },
      // 当前激活的标签页
      activeTab: 'balance',
      // 加载状态控制
      loading: {
        balance: false,
        profit: false,
        cashflow: false
      },
      // 资产负债表数据
      balanceSheetData: [],
      // 利润表数据
      profitSheetData: [],
      // 现金流量表数据
      cashFlowSheetData: [],
      // 资产负债表列配置
      balanceSheetColumns: [
        { prop: '资产总计', label: '资产总计', width: 150 },
        { prop: '负债合计', label: '负债合计', width: 150 },
        { prop: '股东权益合计', label: '股东权益合计', width: 150 },
        { prop: '货币资金', label: '货币资金', width: 150 },
        { prop: '应收账款', label: '应收账款', width: 150 },
        { prop: '存货', label: '存货', width: 150 },
        { prop: '流动资产合计', label: '流动资产合计', width: 150 },
        { prop: '非流动资产合计', label: '非流动资产合计', width: 150 },
        { prop: '短期借款', label: '短期借款', width: 150 },
        { prop: '应付账款', label: '应付账款', width: 150 },
        { prop: '流动负债合计', label: '流动负债合计', width: 150 },
        { prop: '非流动负债合计', label: '非流动负债合计', width: 150 }
      ],
      // 利润表列配置
      profitSheetColumns: [
        { prop: '营业总收入', label: '营业总收入', width: 150 },
        { prop: '营业收入', label: '营业收入', width: 150 },
        { prop: '营业成本', label: '营业成本', width: 150 },
        { prop: '销售费用', label: '销售费用', width: 150 },
        { prop: '管理费用', label: '管理费用', width: 150 },
        { prop: '财务费用', label: '财务费用', width: 150 },
        { prop: '营业利润', label: '营业利润', width: 150 },
        { prop: '利润总额', label: '利润总额', width: 150 },
        { prop: '净利润', label: '净利润', width: 150 },
        { prop: '归属于母公司股东的净利润', label: '归母净利润', width: 150 },
        { prop: '基本每股收益', label: '每股收益', width: 120 }
      ],
      // 现金流量表列配置
      cashFlowSheetColumns: [
        { prop: '经营活动产生的现金流量净额', label: '经营活动现金流净额', width: 180 },
        { prop: '投资活动产生的现金流量净额', label: '投资活动现金流净额', width: 180 },
        { prop: '筹资活动产生的现金流量净额', label: '筹资活动现金流净额', width: 180 },
        { prop: '现金及现金等价物净增加额', label: '现金净增加额', width: 150 },
        { prop: '销售商品、提供劳务收到的现金', label: '销售商品收到现金', width: 180 },
        { prop: '支付给职工以及为职工支付的现金', label: '支付职工现金', width: 150 },
        { prop: '支付的各项税费', label: '支付税费', width: 120 },
        { prop: '购建固定资产、无形资产和其他长期资产支付的现金', label: '购建资产支付现金', width: 200 }
      ]
    }
  },
  mounted() {
    // 组件挂载时加载数据
    this.loadAllData()
  },
  methods: {
    /**
     * 加载所有财务数据
     * 同时加载资产负债表、利润表、现金流量表
     */
    async loadAllData() {
      // 验证股票代码
      if (!this.queryForm.symbol) {
        this.$message.warning('请输入股票代码')
        return
      }
      // 并行加载三种报表数据
      await Promise.all([
        this.loadBalanceSheet(),
        this.loadProfitSheet(),
        this.loadCashFlowSheet()
      ])
    },

    /**
     * 加载资产负债表数据
     * 调用后端API获取资产负债表数据
     */
    async loadBalanceSheet() {
      this.loading.balance = true
      try {
        const params = {
          symbol: this.queryForm.symbol,
          reportType: this.queryForm.reportType
        }
        const response = await financialApi.getBalanceSheet(params)
        this.balanceSheetData = response.data || []
      } catch (error) {
        this.$message.error('加载资产负债表数据失败: ' + error.message)
      } finally {
        this.loading.balance = false
      }
    },

    /**
     * 加载利润表数据
     * 调用后端API获取利润表数据
     */
    async loadProfitSheet() {
      this.loading.profit = true
      try {
        const params = {
          symbol: this.queryForm.symbol,
          reportType: this.queryForm.reportType
        }
        const response = await financialApi.getProfitSheet(params)
        this.profitSheetData = response.data || []
      } catch (error) {
        this.$message.error('加载利润表数据失败: ' + error.message)
      } finally {
        this.loading.profit = false
      }
    },

    /**
     * 加载现金流量表数据
     * 调用后端API获取现金流量表数据
     */
    async loadCashFlowSheet() {
      this.loading.cashflow = true
      try {
        const params = {
          symbol: this.queryForm.symbol,
          reportType: this.queryForm.reportType
        }
        const response = await financialApi.getCashFlowSheet(params)
        this.cashFlowSheetData = response.data || []
      } catch (error) {
        this.$message.error('加载现金流量表数据失败: ' + error.message)
      } finally {
        this.loading.cashflow = false
      }
    },

    /**
     * 处理标签页切换事件
     * @param {Object} tab - 当前激活的标签页对象
     */
    handleTabChange(tab) {
      // 标签页切换时的处理逻辑（如有需要）
      console.log('切换到标签页:', tab.name)
    },

    /**
     * 格式化数值显示
     * 将大数字转换为带单位的字符串（万、亿）
     * @param {Number} value - 需要格式化的数值
     * @returns {String} 格式化后的字符串
     */
    formatNumber(value) {
      if (value === null || value === undefined || value === '-') {
        return '-'
      }
      const num = parseFloat(value)
      if (isNaN(num)) {
        return value
      }
      // 大于1亿时显示为亿元
      if (Math.abs(num) >= 100000000) {
        return (num / 100000000).toFixed(2) + '亿'
      }
      // 大于1万时显示为万元
      if (Math.abs(num) >= 10000) {
        return (num / 10000).toFixed(2) + '万'
      }
      return num.toFixed(2)
    }
  }
}
</script>

<style scoped>
/* 页面主容器样式 */
.stock-financial {
  padding: 0;
}

/* 清除浮动样式 */
.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both;
}

/* 查询表单样式 */
.demo-form-inline {
  margin-bottom: 20px;
}

/* 表格标题样式 */
.table-title {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #303133;
}
</style>
