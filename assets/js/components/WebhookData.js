import React, {Component} from 'react'
import AddRegistry from './../components/AddRegistry'
import Btn from './../components/Btn'
import Msg from './../components/Msg'
import Loader from './../components/Loader'

export default class WebhookData extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	renderControls(){
		return (
			<div className="Controls">

			</div>
		);
	}
	renderData(){
		return (
			<div className="Data">
				{this.renderDataHeaders}
				{this.renderDataBody()}
			</div>
		);
	}
	renderDataHeaders(){
		return (
			<div className="DataHeaders">
				
			</div>
		);
	}
	renderDataBody(){
		return (
			<div className="DataBody">
			
			</div>
		);
	}
	render() {
		return (
			<div className="WebhookData">
				{this.renderControls()}
				{this.renderData()}
			</div>
		);
	}
}

WebhookData.childContextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object
};

WebhookData.contextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object
};
