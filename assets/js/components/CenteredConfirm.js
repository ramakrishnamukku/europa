/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, { Component, PropTypes } from 'react'

export default  class CenteredConfirm extends Component {
  constructor(props) {
    super(props);
    this.state = {};
  }
  renderMsg(){
    if (this.props.message) {
      return (
        <div>
          <span style={this.props.messageStyle || {}}>{this.props.message}</span>
        </div>
      );
    }
  }
  render(){
    return (
      <div className="CenteredDelete">
        {this.renderMsg()}
        <div>
          <div className="ButtonBlue"
               style={this.props.confirmButtonStyle}
               onClick={this.props.onConfirm}>
            {this.props.confirmButtonText}
          </div>
          <div className="ButtonPink"
               onClick={this.props.onCancel}>
            Cancel
          </div>
        </div>
      </div>
    );
  }
}

CenteredConfirm.propTypes = {
  message: React.PropTypes.string,
  confirmButtonText:  React.PropTypes.string,
  confirmButtonStyle: React.PropTypes.object,
  onConfirm: React.PropTypes.func.isRequired,
  onCancel: React.PropTypes.func.isRequired
};

CenteredConfirm.defaultProps = {
  message: "Are you sure?",
  confirmButtonStyle: "Continue"
};



