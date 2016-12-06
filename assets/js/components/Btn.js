import React, {Component} from 'react'

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
		if(this.canClick()) {
			return 'Btn';
		} 

		return 'Btn Grey';
	}
	renderButton(){
		return (
			<div className={this.getClassName()} onClick={() => this.onClick()}>
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
	text: React.PropTypes.string.isRequired,
	onClick: React.PropTypes.func.isRequired,
	color: React.PropTypes.string,
	canClick: React.PropTypes.bool,
	help: React.PropTypes.string
};

Btn.childContextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object
};

Btn.contextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object
};
