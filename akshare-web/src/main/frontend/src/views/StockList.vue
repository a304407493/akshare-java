<template>
  <div class="stock-list">
    <el-card>
      <div slot="header" class="clearfix">
        <span>A股股票列表</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="loadData">
          <i class="el-icon-refresh"></i> 刷新
        </el-button>
      </div>

      <el-input
        v-model="searchText"
        placeholder="搜索股票代码或名称"
        prefix-icon="el-icon-search"
        style="margin-bottom: 20px;"
        clearable
      ></el-input>

      <el-table
        :data="filteredData"
        stripe
        style="width: 100%;"
        height="600"
        v-loading="loading"
      >
        <el-table-column prop="代码" label="代码" width="120"></el-table-column>
        <el-table-column prop="市场" label="市场" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row['市场'] === '1' ? 'danger' : 'success'">
              {{ scope.row['市场'] === '1' ? '上海' : '深圳' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="名称" label="名称"></el-table-column>
        <el-table-column label="操作" width="150">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="primary"
              @click="viewHistory(scope.row)"
            >
              查看K线
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { stockApi } from '../utils/api'

export default {
  name: 'StockList',
  data() {
    return {
      searchText: '',
      tableData: [],
      loading: false
    }
  },
  computed: {
    filteredData() {
      if (!this.searchText) {
        return this.tableData
      }
      const search = this.searchText.toLowerCase()
      return this.tableData.filter(item =>
        item['代码']?.toLowerCase().includes(search) ||
        item['名称']?.toLowerCase().includes(search)
      )
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const response = await stockApi.getStockList()
        this.tableData = response.data || []
      } catch (error) {
        this.$message.error('加载数据失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },
    viewHistory(row) {
      this.$router.push({
        path: '/stock/history',
        query: { symbol: row['代码'] }
      })
    }
  }
}
</script>

<style scoped>
.stock-list {
  padding: 0;
}
.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both;
}
</style>
