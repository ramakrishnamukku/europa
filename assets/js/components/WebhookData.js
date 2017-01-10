/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import Btn from './../components/Btn'
import RadioButton from './../components/RadioButton'
import NPECheck from './../util/NPECheck'
import Msg from '../components/Msg'

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
	redeliverWebhook(){
		let recordId = this.props.webhookData.notificationId;

		this.context.actions.redeliverNotification(recordId)
			.then((res) => {
				let newRecordId = res;
				return this.context.actions.getNotificationRecord(newRecordId)
			})
			.then(this.context.actions.appendNotificationRecord);
	}
	renderControls(){
		return (
			<div className="Controls">
				<div className="Flex1 Title">
					Webhook Data
				</div>
					{this.renderChooseType()}
				<div className="Flex1 Redeliver">
					{this.renderRedeliverButton()}
				</div>
				<div className="Close">
					<i className="icon icon-dis-close" 
					   onClick={ () => this.props.close() }
					/>
				</div>
			</div>
		);
	}
	renderRedeliverButton(){
		let id = this.props.webhookData.notificationId;

		if(id) {
			if(NPECheck(this.context.state, 'notif/redeliverXHRID', false) == id) {
				let errorMsg = NPECheck(this.context.state, 'notif/redeliverError', false);

				if(errorMsg) {
					return (
						<Msg text={errorMsg} close={() => this.context.actions.clearRedeliverError()}/>
					);
				}

				return (
					<i className="icon icon-dis-waiting rotating"/>
				);
			}

			return (
				<Btn onClick={ () => this.redeliverWebhook() }
					 style={{height: '22px', width: '105px', fontSize: '0.75rem'}}
					 text="Redeliver" />
			);
		}
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
				<div className="ScreenCover JustifyCenter">
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
	webhookData: PropTypes.object.isRequired,
	recordId: PropTypes.string,
	close: PropTypes.func,
	modal: PropTypes.bool,
	style: PropTypes.object
};

WebhookData.childContextTypes = {
    actions: PropTypes.object,
    state: PropTypes.object,
    router: PropTypes.object
};

WebhookData.contextTypes = {
    actions: PropTypes.object,
    state: PropTypes.object,
    router: PropTypes.object
};
