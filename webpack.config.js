const path = require('path');
const webpack = require('webpack');

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
        test: /\.js$/,
      },
      {
        test: /\.scss$/,
        loaders: ["style", "css", "sass"]
      },
    ],
  },
  output: {
    path: "./public",
    filename: "[name]",
  },
};

module.exports = compiler;
