<template>
  <div id="app">
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header style="background-color: #409EFF; color: white;">
        <div style="display: flex; align-items: center; height: 100%;">
          <h2 style="margin: 0;">AKShare 金融数据可视化</h2>
          <el-menu
            :default-active="activeMenu"
            mode="horizontal"
            background-color="#409EFF"
            text-color="#ffffff"
            active-text-color="#ffd04b"
            router
            style="margin-left: 50px;"
          >
            <!-- 实时行情 -->
            <el-menu-item index="/stock/realtime">实时行情</el-menu-item>
            
            <!-- 历史K线 -->
            <el-menu-item index="/stock/history">历史K线</el-menu-item>
            
            <!-- 股票名录 -->
            <el-submenu index="/stock/list">
              <template slot="title">股票名录</template>
              <el-menu-item index="/stock/list">基础列表</el-menu-item>
              <el-menu-item index="/stock/list-enhanced">增强列表</el-menu-item>
              <el-menu-item index="/stock/delist">退市股票</el-menu-item>
            </el-submenu>
            
            <!-- 市场概况 -->
            <el-submenu index="/stock/market">
              <template slot="title">市场概况</template>
              <el-menu-item index="/stock/market">基础概况</el-menu-item>
              <el-menu-item index="/stock/market-enhanced">增强概况</el-menu-item>
            </el-submenu>
            
            <!-- 财务数据 -->
            <el-submenu index="/stock/financial">
              <template slot="title">财务数据</template>
              <el-menu-item index="/stock/financial">财务报表</el-menu-item>
              <el-menu-item index="/stock/financial-indicators">财务指标</el-menu-item>
            </el-submenu>
            
            <!-- 股东数据 -->
            <el-submenu index="/stock/holder">
              <template slot="title">股东数据</template>
              <el-menu-item index="/stock/holder">股东持股</el-menu-item>
              <el-menu-item index="/stock/holder-trend">股东趋势</el-menu-item>
            </el-submenu>
            
            <!-- 机构数据 -->
            <el-submenu index="/stock/institution">
              <template slot="title">机构数据</template>
              <el-menu-item index="/stock/institution">龙虎榜</el-menu-item>
              <el-menu-item index="/stock/institution/detail">龙虎榜详情</el-menu-item>
              <el-menu-item index="/stock/institution/rank">机构排行</el-menu-item>
            </el-submenu>
            
            <!-- 竞价数据 -->
            <el-menu-item index="/stock/auction">竞价数据</el-menu-item>
          </el-menu>
        </div>
      </el-header>

      <!-- 主内容区域 -->
      <el-main>
        <router-view></router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script>
export default {
  name: 'App',
  computed: {
    activeMenu() {
      return this.$route.path
    }
  }
}
</script>

<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  height: 100vh;
}

.el-header {
  padding: 0;
}

.el-main {
  background-color: #f5f7fa;
  padding: 20px;
}
</style>
