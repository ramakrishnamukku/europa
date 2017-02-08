/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'

export default class RadioButton extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	onClick(){
		if(this.props.hasOwnProperty('disabled')) {
			if(!this.props.disabled) {
				this.props.onClick();
			}
		} else {
			this.props.onClick();
		}
	}
	renderLabel(){
		if(this.props.label) {
			let className = this.props.labelClassName || '';

			if(!this.props.isChecked) {
				className += ' Inactive';
			}

			return (
				<label className={className}
				       style={{margin: '0', cursor: (this.props.disabled) ? 'initial' : ''}}>
			 		{this.props.label}
			 	</label>
			);
		}
	}
	renderIcon(){
		let className = (this.props.isChecked) ? 'icon icon-dis-radio-check' 
											   : 'icon icon-dis-radio-uncheck Inactive';

		if(this.props.iconClassName) {
			className += ` ${this.props.iconClassName}`;
		}

		return (
			<i className={className} style={{cursor: (this.props.disabled) ? 'initial' : ''}}/>
		);
	}
	render(){
		return (
			<div className="RadioButton FlexRow" onClick={() => this.onClick()}>
				{this.renderIcon()}
				{this.renderLabel()}
			</div>
		);
	}
}

RadioButton.propTypes = {
	label: PropTypes.string,
	onClick: PropTypes.func.isRequired,
	isChecked: PropTypes.bool.isRequired,
	disabled: PropTypes.bool,
	iconClassName: PropTypes.string,
	labelClassName: PropTypes.string
};


