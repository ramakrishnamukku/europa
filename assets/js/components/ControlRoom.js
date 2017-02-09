/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, { Component, PropTypes } from 'react'

export default class ControlRoom extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	componentDidMount() {
		if(this.props.componentDidMount && typeof this.props.componentDidMount == 'function') {
			this.props.componentDidMount.call(this);
		}
	}
	renderHeader() {
		if(this.props.renderHeaderContent) {
			return (
				<div className="Header" style={this.props.headerStyle || {}}>
					{this.props.renderHeaderContent()}
				</div>
			);
		}
	}
	renderBody() {
		return (
			<div className="Body" style={this.props.bodyStyle || {}}>
				{this.props.renderBodyContent()}
			</div>
		);
	}
	render() {
		return (
			<div className="ControlRoom">
				{this.renderHeader()}
				{this.renderBody()}
			</div>
		);
	}
}

ControlRoom.propTypes = {
	renderBodyContent: PropTypes.func.isRequired,
	bodyStyle: PropTypes.object,
	bodyClassName: PropTypes.string,
	renderHeaderContent: PropTypes.func,
	headerStyle: PropTypes.object,
	headerClassName: PropTypes.string,
};
