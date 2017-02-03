/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import Loader from './../components/Loader'
import NPECheck from './../util/NPECheck'
import CopyToClipboard from './../util/CopyToClipboard'
import CenteredConfirm from './../components/CenteredConfirm'
import WebhookData from './../components/WebhookData'
import AddRepoNotification from './../components/AddRepoNotification'

let notifTargetKey = 'target';
let notifSecretKey= 'secret';

export default class RepoNotifications extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	deleteNotification(){
		this.context.actions.deleteNotification()
			.then(() => {
				let repoId = NPECheck(this.props, 'repoDetails/activeRepo/id', null);
				this.context.actions.listRepoNotifications(repoId, true);			
			});
	}
	getNotificationItemClassName(status){
		let className = 'RepoNotificationListItem ';
		switch(status) {
			case 'SUCCESS':
				return (className += 'SuccessBg');
			break;

			case 'ERROR':
				return (className += 'ErrorBg');
			break;

			case 'WARNING':
				return (className += 'WarningBg');
			break;

			default:
				return className;
		}
	}
	renderAddNotification(){
		return (
			<AddRepoNotification {...this.props} isExistingRepo={true}/>
		);
	}
	renderRepoNotifications(){
		let label = `Existing Notifications (${this.props.notifs.length})`;

		return (
			<div className="RepoNotificationsList">
				<label className="small">{label}</label>
				{this.props.notifs.map((notif, index) => this.renderRepoNotificationItem(notif, index))}
			</div>
		);
	}
	renderRepoNotificationItem(notif, index){
		let testInfo = NPECheck(this.props, `notif/testExistingNotification/${notif.id}`, {});
		
		return (
			<div key={index} className={this.getNotificationItemClassName(testInfo.status)}>
				<div className="Info">
					<span className="Cell">
						<span className="Label">
							Target:
						</span>
						<span className="Value" id={notif.id}>
							{notif.target}
						</span>
					</span>
					<div className="NotifActions">
						{this.renderTestNotificationStatus(testInfo.status, testInfo.responseCode, notif.id)}
						<i className={testInfo.XHR ? 'icon icon-dis-waiting rotating' : 'icon icon-dis-output'}
						    onClick={() => this.context.actions.testExistingNotification(notif)}
						   data-tip="Test Notification" 
						   data-for="ToolTipTop"/>
						<i className="icon icon-dis-download" 
						   onClick={() => CopyToClipboard(document.getElementById(notif.id))}
						   data-tip="Copy URL" 
						   data-for="ToolTipTop"/>
						<i className="icon icon-dis-trash" 
						   onClick={() => this.context.actions.toggleRepoNotificationForDelete(notif.id)}
						   data-tip="Delete Notificiation" 
						   data-for="ToolTipTop"/>
					</div>
				</div>
				{this.renderDeleteNotification(notif.id)}
				{this.renderWebhookData(testInfo, notif.id)}
			</div>
		);
	}
	renderTestNotificationStatus(status, statusCode, notificationId){
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
			<div className="NotificationTestActions" style={{margin: '0'}}>
				<div className={className}>
					<span className="StatusText">{statusText}</span>&nbsp;
					<span className="StatusCode">{ (statusCode) ? `(${statusCode})` : null}</span>&nbsp;
					<span className="ViewTestResults" 
						  onClick={() => this.context.actions.toggleShowExistingNotificationTestResults(notificationId)}>
						{ (statusCode) ? ' - View Details' : null}
					</span>
				</div>
			</div>
		);
	}
	renderDeleteNotification(notifId){
		let activeId = NPECheck(this.props, 'notif/deleteNotifId', null);

		if( activeId == notifId) {

			if(NPECheck(this.props, 'notif/deleteNotificationXHR', false)) {
				return (
					<Loader />
				);
			}

			return (
				<CenteredConfirm message="Are you sure you want to delete this notification?"
							     confirmButtonText="Delete"
							     confirmButtonStyle={{}}
							     onConfirm={() => this.deleteNotification() }
							     onCancel={() => this.context.actions.toggleRepoNotificationForDelete()}/>
			);
		}
	}
	renderWebhookData(testInfo, notificationId){
		if(testInfo.displayWebhookData) {
			return (
				<WebhookData webhookData={testInfo.testNotification} 
							 close={ () => this.context.actions.toggleShowExistingNotificationTestResults(notificationId) }/>
			)
		};
	}
	render() {	
		return (
			<div className="RepoNotifications">
				<div className="FlexRow">
					<label>Notifications</label>
				</div>
				{this.renderAddNotification()}
				{this.renderRepoNotifications()}
			</div>	
		);
	}
}

RepoNotifications.propTypes =  {
	notifs: PropTypes.array.isRequired
};

RepoNotifications.childContextTypes = {
	actions: PropTypes.object
};

RepoNotifications.contextTypes = {
	actions: PropTypes.object
};
