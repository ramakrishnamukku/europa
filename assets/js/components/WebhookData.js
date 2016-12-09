import React, {Component} from 'react'
import AddRegistry from './../components/AddRegistry'
import Btn from './../components/Btn'
import Msg from './../components/Msg'
import Loader from './../components/Loader'
import RadioButton from './../components/RadioButton'

export default class WebhookData extends Component {
	constructor(props) {
		super(props);
		this.state = {
			viewingType: 'Request',
			activeData: this.props.webhookData.request

		};
	}
	changeType(newType){
		this.setState({
			viewingType: newType,
			activeData: (newType == 'Request') ? this.props.webhookData.request : this.props.webhookData.response
		});
	}
	renderControls(){
		return (
			<div className="Controls">
				<div className="Flex1 Title">
					Webhook Data
				</div>
					{this.renderChooseType()}
				<div className="Flex1 Redeliver">
					<Btn onClick={() => console.log('todo')}
						 text="Redeliver"
						 />
				</div>
			</div>
		);
	}
	renderChooseType(){
		let isRequest = this.state.viewingType == 'Request';
		let activeClassName = 'icon icon-dis-radio-check';
		let inActiveClassName = 'icon icon-dis-radio-uncheck';
		let requestClassName, responseClassName;

		if(isRequest) {
			requestClassName = activeClassName;
			responseClassName = inActiveClassName
		} else {
			requestClassName = inActiveClassName;
			responseClassName =  activeClassName;
		}

		return (
			<div className="Flex1 ChooseType">
				<div className="">
					<RadioButton onClick={() => this.changeType('Request')} 
								 isChecked={this.state.viewingType == 'Request'}
								 label="Request" />
				</div>
				<div className="">
					<RadioButton onClick={() => this.changeType('Response')} 
								 isChecked={this.state.viewingType == 'Response'}
								 label="Response"/>
				</div>
			</div>
		);
	}
	renderData(){
		return (
			<div className="Data">
				{this.renderDataHeaders()}
				{this.renderDataBody()}
			</div>
		);
	}
	renderDataHeaders(){
		let headers = this.state.activeData.headers;

		return (
			<div className="DataHeaders">
			     <div className="Title">
					{this.state.viewingType}&nbsp;Headers
				</div>
				<pre>
					{JSON.stringify(headers, null, 4)}
				</pre>
			</div>
		);
	}
	renderDataBody(){
		let body = this.state.activeData.body;
		return (
			<div className="DataBody">
				<div className="Title">
					{this.state.viewingType}&nbsp;Body
				</div>
				<pre>
					{JSON.stringify(JSON.parse(body), null, 2)}
				</pre>
			</div>
		);
	}
	render() {

		if(!this.props.webhookData || !Object.keys(this.props.webhookData).length) {
			return (
				<div className="WebhookData">
					No Webhook Data
				</div>
			);		
		}

		return (
			<div className="WebhookData">
				{this.renderControls()}
				{this.renderData()}
			</div>
		);


	}
}

WebhookData.propTypes = {
	webhookData: React.PropTypes.object.isRequired
};

WebhookData.childContextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object
};

WebhookData.contextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object
};
