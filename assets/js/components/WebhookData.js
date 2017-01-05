/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component} from 'react'
import Btn from './../components/Btn'
import RadioButton from './../components/RadioButton'

export default class WebhookData extends Component {
	constructor(props) {
		super(props);
		this.state = {
			viewingType: 'Request',
			activeData: props.webhookData.request
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
					<Btn onClick={ () => console.log('todo') }
						 style={{height: '22px', width: '105px', fontSize: '0.75rem'}}
						 text="Redeliver" />
				</div>
				<div className="Close">
					<i className="icon icon-dis-close" 
					   onClick={ () => this.props.close() }
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
				<div className="Headers">
					{Object.keys(headers).map((key, index) => {
						return (
							<div className="FlexRow" key={index}>
								<span className="Key">{key}:&nbsp;</span>
								<span className="Value">{headers[key]}</span>
							</div>
						);
					})}
				</div>
			</div>
		);
	}
	renderDataBody(){
		let body = this.state.activeData.body;
		let content = body;

		try {
			content = JSON.stringify(JSON.parse(body), null, 2)
		} catch(e){

		}

		return (
			<div className="DataBody">
				<div className="Title">
					{this.state.viewingType}&nbsp;Body
				</div>
				<pre>
					{content}
				</pre>
			</div>
		);
	}
	render() {
		if(!this.props.webhookData || !Object.keys(this.props.webhookData).length) {
			return (
				<div className="WebhookData" style={this.props.style || {}}>
					<span className="RedColor">Failed to load webhook data</span>
				</div>
			);		
		}

		let className = 'WebhookData';

		if(this.props.modal) {
			className += ' Modal';

			return (
				<div className="ScreenCover JustifyCenter AlignCenter">
					<div className={className}>
						{this.renderControls()}
						{this.renderData()}
					</div>
				</div>	
			);
		}

		return (
			<div className={className}>
				{this.renderControls()}
				{this.renderData()}
			</div>
		);


	}
}

WebhookData.propTypes = {
	webhookData: React.PropTypes.object.isRequired,
	close: React.PropTypes.func,
	modal: React.PropTypes.bool,
	style: React.PropTypes.object
};

WebhookData.childContextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};

WebhookData.contextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};
