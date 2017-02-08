/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'

export default class Checkbox extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	renderLabel(){
		if(this.props.label) {
			let className = this.props.labelClassName || '';

			if(!this.props.isChecked) {
				className += ' Inactive';
			}

			return (
				<label className={className}
				       style={{margin: '0'}}>
			 		{this.props.label}
			 	</label>
			);
		}
	}
	renderIcon(){
		let className = (this.props.isChecked) ? 'icon icon-dis-box-check' 
											   : 'icon icon-dis-box-uncheck Inactive';

		if(this.props.iconClassName) {
			className += ` ${this.props.iconClassName}`;
		}

		return (
			<i className={className} />
		);
	}
	render(){
		return (
			<div className="RadioButton FlexRow" onClick={() => this.props.onClick()}>
				{this.renderIcon()}
				{this.renderLabel()}
			</div>
		);
	}
}

Checkbox.propTypes = {
	label: PropTypes.string,
	onClick: PropTypes.func.isRequired,
	isChecked: PropTypes.bool.isRequired,
	iconClassName: PropTypes.string,
	labelClassName: PropTypes.string
};


