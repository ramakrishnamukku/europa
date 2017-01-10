/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import Loader from './../components/Loader'
import NPECheck from './../util/NPECheck'
import CopyToClipboard from './../util/CopyToClipboard'
import CenteredConfirm from './../components/CenteredConfirm'
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
				let repoId = NPECheck(this.context.state, 'repoDetails/activeRepo/id', null);
				this.context.actions.listRepoNotifications(repoId, true);			
			});
	}
	renderAddNotification(){
		return (
			<AddRepoNotification isExistingRepo={true}/>
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
		return (
			<div key={index} className="RepoNotificationListItem">
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
						<i className="icon icon-dis-download" 
						   onClick={() => CopyToClipboard(document.getElementById(notif.id))}
						   data-tip="Copy URL" 
						   data-for="ToolTipTop"/>
						<i className="icon icon-dis-output"
						    
						   data-tip="Test Notification" 
						   data-for="ToolTipTop"/>
						<i className="icon icon-dis-trash" 
						   onClick={() => this.context.actions.toggleRepoNotificationForDelete(notif.id)}
						   data-tip="Delete Notificiation" 
						   data-for="ToolTipTop"/>
					</div>
				</div>
				{this.renderDeleteNotification(notif.id)}
			</div>
		);
	}
	renderDeleteNotification(notifId){
		let activeId = NPECheck(this.context.state, 'notif/deleteNotifId', null);

		if( activeId == notifId) {

			if(NPECheck(this.context.state, 'notif/deleteNotificationXHR', false)) {
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
	render() {	
		if(NPECheck(this.context.state, 'notif/notifsXHR', false)) {
			return (
				<Loader />
			);
		}

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
	actions: PropTypes.object,
    state: PropTypes.object,
    router: PropTypes.object
};

RepoNotifications.contextTypes = {
	actions: PropTypes.object,
    state: PropTypes.object,
    router: PropTypes.object
};
