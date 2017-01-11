/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import WebhookViewer from './../components/WebhookViewer'
import Loader from './../components/Loader'
import TimelineIcons from './../util/TimelineIcons'
import ConvertTimeUTC from './../util/ConvertTimeUTC'
import ConvertTimeFriendly from './../util/ConvertTimeFriendly'
import NPECheck from './../util/NPECheck'
import Msg from './../components/Msg'

export default class RepoEventItem extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	viewNotificationInfo(event){
		this.context.actions.toggleEventDetails(event.id).then((getNotifRecords) => {
			if(getNotifRecords) this.context.actions.getEventNotificationRecords(event.notifications);
		});
	}
	renderEventData(event){
		if(this.props.repoDetails.activeEventId == event.id) {

			let errorMsg = NPECheck(this.props, 'notif/retrieveNotifRecordsError', false);

			if(errorMsg) {
				return (
					<Msg text={errorMsg} 
						 close={() => this.context.actions.clearNotifRecordsError()}
						 style={{marginBottom: '14px'}}/>
				);
			}

			if(NPECheck(this.props, 'notif/notifRecordXHR', false)) {
				return (
					<Loader />
				);
			}

			return (
				<WebhookViewer {...this.props} 
							   allWebhookData={this.props.notif.currentNotifRecords} />
			);
		} 
	}
	renderWebhookText(event){
		let action = this.viewNotificationInfo.bind(this, event);
		let notifLength = event.notifications.length;
		let verb = (this.props.repoDetails.activeEventId == event.id) ? 'Hide' : 'View';
		let inside = (notifLength == 1) ? `${verb} Webhook (${notifLength})` : `${verb} Webhooks (${notifLength})`;
		let className = "Item";

		if(notifLength == 0) {
			action = () => {};
			inside = 'No Webhooks'
			className = "Item NoClick"
		}

		return (
			<span className={className} onClick={ () =>  action()}>{inside}</span>
		);
	}
	render() {
		let event = this.props.event;
		let time = event.eventTime;
		let friendlyTime = ConvertTimeFriendly(time);
		let timeUTC = ConvertTimeUTC(new Date(time), true);
		let SHA = event.imageSha;
		let activeRepo = NPECheck(this.props, 'repoDetails/activeRepo', {});
		let name = activeRepo.name

		try {
			if(name.indexOf('/') > -1) {
				console.log('true')
				name = name.substr(name.indexOf('/') + 1);
			}
		} catch(e){
			console.log('hit');
			name = activeRepo.name;
		}

		return (
			<div className="RepoEventContainer">
				<div className="RepoEventItem">
					<div className="EventLine">
						<img className="EventIcon" src={TimelineIcons(event.eventType)}/>
					</div>
					<div className="EventDetails">
						<div className="EventType">
							<div className="Image">
								Image
							</div>
							<div className="Type">
								{event.eventType}
							</div>
						</div>
						<div className="Time">
						    <span className="Friendly">{friendlyTime}</span>
							<span className="UTC">{timeUTC}</span>
						</div>
						<div className="ImageInfo">
							<span className="Image">
								<i className="icon icon-dis-push" />
								<span className="Label">Push Image:&nbsp;</span>
								<span className="Value">{name}</span>
							</span>
							<span className="Sha">
								<i className="icon icon-dis-blank" />
								<span className="Label">SHA:&nbsp;</span>
								<span className="Value">{SHA}</span></span>
							<span className="Tags">
								<i className="icon icon-dis-blank" />
								{event.imageTags.map((tag, index) => {
									return (
										<span className="Tag" key={index}>{tag}</span>
									);
								})}
							</span>
						</div>
						<div className="Notifications">
							{this.renderWebhookText(event)}
						</div>
					</div>
				</div>
				{this.renderEventData(event)}
			</div>
		);
	}	
}

RepoEventItem.propTypes = {
	event: PropTypes.object.isRequired
};

RepoEventItem.childContextTypes = {
    actions: PropTypes.object
};

RepoEventItem.contextTypes = {
    actions: PropTypes.object
};
