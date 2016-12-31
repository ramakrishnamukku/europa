/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import Msg from './../components/Msg'
import Btn from './../components/Btn'
import Loader from './../components/Loader'
import isEmpty from './../util/IsEmpty'
import NPECheck from './../util/NPECheck'
import CopyToClipboard from './../util/CopyToClipboard'
import CenteredConfirm from './../components/CenteredConfirm'

export default class RepoNotifications extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	renderAddNotification(){
		return (
			<div className="AddNotification">
				<div className="FlexRow">
					<div className="Flex1 FlexColumn">
						<label className="small">Webhook URL</label>
						<input className="BlueBorder White FullWidth" 
							   placeholder="Enter Webhook URL"/>
					</div>
					<div className="Flex1 FlexColumn">
						<label className="small">Webhook Secret</label>
						<input className="BlueBorder White FullWidth" 
						       placeholder="Enter Webhook Secret"/>
					</div>
				</div>
				<Btn onClick={() => console.log('some shit to add')}
					 text="Add Notification"
				     canClick={true} />
			</div>
		);
	}
	renderRepoNotifications(){
		return (
			<div className="RepoNotificationsList">
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
		let activeId = NPECheck(this.context.state, 'repoDetails/deleteNotifId', null);

		if( activeId == notifId) {
			return (
				<CenteredConfirm message="Are you sure you want to delete this notification?"
							     confirmButtonText="Delete"
							     confirmButtonStyle={{}}
							     onConfirm={() => this.context.actions.deleteNotification()}
							     onCancel={() => this.context.actions.toggleRepoNotificationForDelete()}/>
			);
		}
	}
	render() {	
		if(NPECheck(this.context.state, 'repoDetails/notifsXHR', false)) {
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
