/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, { Component, PropTypes } from 'react'

export default class ContentRow extends Component {
  constructor(props) {
    super(props);
    this.state = {};
  }
  renderBody(column) {
    if(column.renderBody && typeof column.renderBody == 'function') {
      return column.renderBody()
    }
  }
  renderIcon(column) {
    if(!column.icon) return;

    return (
      <div className="IconContainer">
          <i className={column.icon} style={(column.iconStyle) ? column.iconStyle : {}}></i>
      </div>
    );
  }
  render() {
    if(!this.props.row.columns) {
      return (
        <span></span>
      );
    }

    let toRender = this.props.row.columns.filter((column) => {
        return (column.hasOwnProperty('condition') && !column.condition) ? false : true;
    });

    if(!toRender.length) {
      return (
        <span></span>
      );
    }
    
    return (
      <div className="ContentRow" style={(this.props.style) ? this.props.style : {}}>
        {toRender.map((column, index) => {
          return (
            <div className="ContentColumn" key={index}>
              {this.renderIcon(column)}
              <div className="ContentBody" style={(this.props.bodyStyle) ? this.props.bodyStyle : {}}>
                {this.renderBody(column)}
              </div>
            </div>
          );
        })}
      </div>
    );
  }
}

ContentRow.propTypes = {
  row: PropTypes.object.isRequired,
  style: PropTypes.object,
  bodyStyle: PropTypes.object
};


