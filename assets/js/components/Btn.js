/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'

export default class Btn extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	canClick(){
		return !(this.props.hasOwnProperty('canClick') && !this.props.canClick)
	}
	onClick(){
		if(this.props.onClick && this.canClick()) {
			this.props.onClick()
		}
	}
	getClassName(){

		if(this.props.className) {
			return this.props.className;
		}

		if(this.canClick()) {
			return 'Btn';
		} 

		return 'Btn Grey';
	}
	renderButton(){
		return (
			<div style={this.props.style || {}} className={this.getClassName()} onClick={() => this.onClick()}>
				{this.props.text}
			</div>
		);
	}
	render() {
		if (!this.props.help) {
			return this.renderButton()
		}

		return (
			<span className="BtnContainer">
				{this.renderButton()}
				<p className="HelperText">{this.props.help}</p>
			</span>
		);
		
	}
}

Btn.propTypes = {
	text: PropTypes.string.isRequired,
	onClick: PropTypes.func.isRequired,
	color: PropTypes.string,
	canClick: PropTypes.bool,
	help: PropTypes.string,
	style: PropTypes.object,
	className: PropTypes.string
};
