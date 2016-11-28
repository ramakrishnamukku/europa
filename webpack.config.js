const path = require('path');
const webpack = require('webpack');
const ExtractTextPlugin = require('extract-text-webpack-plugin');

const compiler = {
  entry: {
    'js/app.js': path.resolve(__dirname, 'js', 'app.js'),
    'css/app.css': path.resolve(__dirname, 'scss', 'app.scss')
  },
  module: {
    loaders: [
      {
        exclude: /node_modules/,
        loader: 'babel',
        test: /\.(jsx|js)$/,
      },
      {
          test: /\.scss$/,
          loader: ExtractTextPlugin.extract('css!sass')
      },
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
