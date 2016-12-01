const path = require('path');
const webpack = require('webpack');
const ExtractTextPlugin = require('extract-text-webpack-plugin');

const compiler = {
  entry: {
    'js/app.js': path.resolve(__dirname, 'assets', 'js', 'app.js'),
    'css/app.css': path.resolve(__dirname, 'assets', 'scss', 'app.scss')
  },
  module: {
    loaders: [{
        exclude: /node_modules/,
        loader: 'babel',
        test: /\.(jsx|js)$/,
      }, {
        test: [/\.scss$/, /\.css$/],
        loader: ExtractTextPlugin.extract('css!sass')
      },
      {
        test: /\.(png|woff|woff2|eot|ttf|svg)$/,
        loader: 'url-loader?limit=100000'
      }
    ],
  },
  output: {
    path: "./public",
    filename: "[name]",
  },
  plugins: [
    new ExtractTextPlugin('[name]', {
      allChunks: true
    })
  ]
};

module.exports = compiler;