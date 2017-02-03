import React, { Component, PropTypes } from 'react'

export default class AccessDenied extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	render(){
		return (
			<div className="AccessDenied">
				<i className="icon icon-dis-alert" />
				Access Denied
			</div>
		);
	}
}