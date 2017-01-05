/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import Msg from './../components/Msg'
import Btn from './../components/Btn'
import Loader from './../components/Loader'
import WebhookData from './../components/WebhookData'
import NPECheck from './../util/NPECheck'
import Dropdown from './../components/Dropdown'

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
		let hasSelector = this.context.state.notif.errorFields.includes(selector)

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
				<div className="FlexColumn">
					{this.renderError()}
					<Btn onClick={() => this.addNotification() }
						 className="Btn"
						 style={{margin: '0 auto', width: '300px', marginTop: '1rem'}}
						 text="Add Notification"
					     canClick={true} />
				</div>
			);
		}
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
	renderError(){
		let errorMsg = NPECheck(this.context.state, 'notif/notifError', '');

		if(errorMsg) {
			return (
				<Msg
					text={errorMsg}
					close={() => this.context.actions.clearNotifError()}
				/>
			);
		} 
	}
	render() {	
		let webhookData = NPECheck(this.context.state, 'notif/testNotification', {});
		return (
			<div>
				{this.renderWebhookData(webhookData)}
				{this.renderAddNotification()}
				{(this.props.isExistingRepo) ? null : this.renderError()}
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
