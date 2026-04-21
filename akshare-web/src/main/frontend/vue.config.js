module.exports = {
  devServer: {
    port: 9876,
    proxy: {
      '/api': {
        target: 'http://localhost:8765',
        changeOrigin: true
      }
    }
  },
  lintOnSave: false,
  productionSourceMap: false
}
