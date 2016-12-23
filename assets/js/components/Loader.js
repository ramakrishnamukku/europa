/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, { Component } from 'react';

export default class Loader extends Component {
  render(){
      return (
        <div className="small-spinner">
          <div className="bounce1">&#x2B22;</div>
          <div className="bounce2">&#x2B22;</div>
          <div className="bounce3">&#x2B22;</div>
        </div>
      );
  }
}
