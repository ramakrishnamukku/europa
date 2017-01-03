/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import Msg from './../components/Msg'
import Btn from './../components/Btn'
import Loader from './../components/Loader'
import WebhookData from './../components/WebhookData'
import NPECheck from './../util/NPECheck'

let notifTargetKey = 'target';
let notifSecretKey= 'secret';

export default class AddRepoNotification extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	addNotification(){
		this.context.actions.addRepoNotification()
			.then(() => {
				let repoId = NPECheck(this.context.state, 'repoDetails/activeRepo/id', null);
				this.context.actions.listRepoNotifications(repoId, true);
			});
	}
	inputClassName(selector){
		// let hasSelector = this.context.state.addRepo.errorFields.includes(selector)
		let hasSelector = false;
		let className;
		if(hasSelector) {
			className = "BlueBorder FullWidth Error";
		} else {
		    className = "BlueBorder FullWidth";
		}

		if(this.props.isExistingRepo) {
			className += ' White'
		}

		return className
	}
	renderAddNotification(){
		let webhookData = this.context.state.notif.testNotification;
		let statusCode = NPECheck(webhookData, 'response/httpStatusCode', null);
		let status = NPECheck(this.context.state, 'notif/testNotificationStatus', null);
		let classNameTarget = this.inputClassName(notifTargetKey);
		let classNameSecret = this.inputClassName(notifSecretKey);

		if(status == 'SUCCESS') classNameTarget += ' SuccessBg';
		if(status == 'ERROR') classNameTarget += ' ErrorBg';


		return (
			<div className="AddNotification">
				<div className="FlexColumn">
					<div className="Flex1 FlexColumn Row">
						<label className="small">Webhook URL</label>
						<div className="FlexRow">
							<input className={classNameTarget}
								   onChange={(e) => this.context.actions.updateNewNotificationField(notifTargetKey, e, false)}
								   value={NPECheck(this.context.state, `notif/newNotification/${notifTargetKey}`, '')}
								   placeholder="Enter Webhook URL"/>
							<div>
								{this.renderTestNotificationStatus(status, statusCode)}
							</div>
							{this.renderTestNotificationButton()}
						</div>
					</div>
					<div className="Flex1 FlexColumn Row">
						<label className="small">Webhook Secret</label>
						<input className={classNameSecret}
							   onChange={(e) => this.context.actions.updateNewNotificationField(notifSecretKey, e, false)}
							   value={NPECheck(this.context.state, `notif/newNotification/${notifSecretKey}`, '')}
						       placeholder="Enter Webhook Secret"/>
					</div>
					
				</div>
				{this.renderAddNotificationButton()}
			</div>
		);
	}
	renderTestNotificationButton(){
		return (
			<Btn className="Btn"
				 onClick={() => this.context.actions.testNotification()}
				 text="Test Webhook"
				 canClick={true}/>
		);
	}
	renderAddNotificationButton(){
		if(this.props.isExistingRepo) {
			if(this.context.state.notif.addNotifXHR) {
				return (
					<Loader />
				);
			}

			return (
				<Btn onClick={() => this.addNotification() }
					 className="Btn"
					 style={{margin: '0 auto', width: '300px', marginTop: '1rem'}}
					 text="Add Notification"
				     canClick={true} />
			);
		}
	}
	renderWebhook(){
		let webhookData = this.context.state.notif.testNotification;
		let statusCode = NPECheck(webhookData, 'response/httpStatusCode', null);
		let status = NPECheck(this.context.state, 'notif/testNotificationStatus', null);
		let className = this.inputClassName(targetKey);

		if(status == 'SUCCESS') className += ' SuccessBg';
		if(status == 'ERROR') className += ' ErrorBg';

		return (
			<div className="">
				<div className="Row FlexColumn">
					<label>
						Webhook URL
					</label>
					<div className="FlexRow">
						
							<input className={className} 
							       value={this.context.state.addRepo.newRepo[targetKey]}
								   placeholder="Enter Webhook URL.."
							       onChange={(e) => this.context.actions.updateNewRepoField(targetKey, e)} />
						<div>
							{this.renderTestNotificationStatus(status, statusCode)}
						</div>
						{this.renderTestNotification()}
						
					</div>
				</div>
				<div className="Row FlexColumn">
					<label>
						Secret (optional)
					</label>
					<input className={this.inputClassName(secretKey)} 
					       value={this.context.state.addRepo.newRepo[secretKey]}
						   placeholder="Enter Secret"
					       onChange={(e) => this.context.actions.updateNewRepoField(secretKey, e)} />
				</div>
			</div>
		);
	}
	renderTestNotificationStatus(status, statusCode){
		let icon = 'icon icon-dis-blank';
		let statusText = "See Test Results Here";
		let className = "InActive";

		if (status == 'SUCCESS') {
			icon = 'icon icon-dis-check'
			statusText = 'Success';
			className="Success";
		}

		if(status == 'WARNING') {
			icon = 'icon icon-dis-warning';
			statusText = "Warning";
			className="Warning";
		}

		if(status == 'ERROR') {
			icon = "icon icon-dis-alert";
			statusText = "Error";
			className = "Error";
		}

		className = "Status " + className;

		return (
			<div className="NotificationTestActions">
				<div className={className}>
					<span className="StatusText">{statusText}</span>&nbsp;
					<span className="StatusCode">{ (statusCode) ? `(${statusCode})` : null}</span>&nbsp;
					<span className="ViewTestResults" 
						  onClick={() => this.context.actions.toggleShowNotificationTestResults()}>
						{ (statusCode) ? ' - View Details' : null}
					</span>
				</div>
			</div>
		);
	}
	renderWebhookData(webhookData){
		if(this.context.state.notif.showNotificationTestResults) {
			return (
				<WebhookData webhookData={webhookData} 
							 modal={true}
							 style={{width: '800px'}}
							 close={ () => this.context.actions.toggleShowNotificationTestResults() }/>
			)
		};
	}
	render() {	
		let webhookData = NPECheck(this.context.state, 'notif/testNotification', {});
		return (
			<div>
				{this.renderWebhookData(webhookData)}
				{this.renderAddNotification()}
			</div>
		);
	}
}

AddRepoNotification.propTypes =  {};

AddRepoNotification.childContextTypes = {
	actions: PropTypes.object,
    state: PropTypes.object,
    router: PropTypes.object
};

AddRepoNotification.contextTypes = {
	actions: PropTypes.object,
    state: PropTypes.object,
    router: PropTypes.object
};
