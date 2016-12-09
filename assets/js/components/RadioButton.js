import React, {Component, PropTypes} from 'react'
import ContentRow from './../components/ContentRow'
import Loader from './../components/Loader'
import Btn from './../components/Btn'
import Msg from './../components/Msg'


export default class RadioButton extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	renderLabel(){
		if(this.props.label) {
			return (
				<label className={this.props.labelClassName}
				       style={{margin: '0'}}>
			 		{this.props.label}
			 	</label>
			);
		}
	}
	renderIcon(){
		let className = (this.props.isChecked) ? 'icon icon-dis-radio-check' : 'icon icon-dis-radio-uncheck';

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

RadioButton.propTypes = {
	label: PropTypes.string,
	onClick: PropTypes.func.isRequired,
	isChecked: PropTypes.bool.isRequired,
	iconClassName: PropTypes.string,
	labelClassName: PropTypes.string
};


