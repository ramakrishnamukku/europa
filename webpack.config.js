const path = require('path');
const webpack = require('webpack');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin')

var plugins = [
  new ExtractTextPlugin('[name]', {
    allChunks: true
  }),
  new webpack.ProvidePlugin({
    Promise: 'imports?this=>global!exports?global.Promise!es6-promise',
    fetch: 'imports?this=>global!exports?global.fetch!whatwg-fetch'
  }),
  new CopyWebpackPlugin([{
    from: 'assets/images/favicon.png',
    to: ''
  }, {
    from: 'assets/images',
    to: 'images'
  }])
]

var IS_PRODUCTION = process.env.NODE_ENV == "production"

if (IS_PRODUCTION) {
  plugins.push(
    new webpack.DefinePlugin({
      'process.env': {
        'NODE_ENV': JSON.stringify('production')
      }
    }),
    new webpack.optimize.UglifyJsPlugin({
      compress: {
        warnings: false
      }
    })
  )
}

var jsBundle = IS_PRODUCTION ? 'js/app.min.js' : 'js/app.js'
var cssBundle = IS_PRODUCTION ? 'css/app.min.css' : 'css/app.css'

var entries = {}
entries[jsBundle] = path.resolve(__dirname, 'assets', 'js', 'app.js')
entries[cssBundle] = path.resolve(__dirname, 'assets', 'scss', 'app.scss')

const compiler = {
  resovle: {
    alias: {}
  },
  entry: entries,
  module: {
    loaders: [{
      exclude: /node_modules/,
      loader: 'babel',
      test: /\.(jsx|js)$/
    }, {
      test: [/\.scss$/, /\.css$/],
      loader: ExtractTextPlugin.extract('css!sass')
    }, {
      test: /\.(png|woff|woff2|eot|ttf|svg)$/,
      loader: 'url-loader?limit=100000'
    }],
  },
  output: {
    path: "./public",
    filename: "[name]",
  },
  plugins: plugins
};

module.exports = compiler;