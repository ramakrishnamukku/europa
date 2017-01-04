/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import WebhookData from './../components/WebhookData'
import Loader from './../components/Loader'
import TimelineIcons from './../util/TimelineIcons'
import ConvertTimeUTC from './../util/ConvertTimeUTC'
import ConvertTimeFriendly from './../util/ConvertTimeFriendly'


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
		if(this.context.state.repoDetails.activeEventId == event.id) {

			if(this.context.state.notif.notifRecordXHR) {
				return (
					<Loader />
				);
			}

			let style = {
				width: 'calc(100% - 48px)',
				margin: '10px auto'
			}; 

			let currentRecords = this.context.state.notif.currentNotifRecords;

			if(!currentRecords.length) {
				return (
					<div className="NotificationContainer">
						<WebhookData  webhookData={{}} style={style} modal={false}/>
					</div>
				);
			}

			return (
				<div className="NotificationContainer FlexColumn">
					{currentRecords.map((record, index) => {
						return (
							<WebhookData key={index} webhookData={record} style={style} modal={false}/>
						);
					})}
				</div>
			);
		} 
	}
	render() {
		let event = this.props.event;
		let time = event.eventTime;
		let friendlyTime = ConvertTimeFriendly(time);
		let timeUTC = ConvertTimeUTC(new Date(time), true);
		let SHA = event.imageSha;
		let notifLength = event.notifications.length;

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
								<span className="Value">Image</span>
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
							<span className="Item" onClick={ () => this.viewNotificationInfo(event) }>{(notifLength > 1) ? `View Webhooks (${notifLength})` : `View Webhook (${notifLength})`}</span>
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
    actions: PropTypes.object,
    state: PropTypes.object,
    router: PropTypes.object
};

RepoEventItem.contextTypes = {
    actions: PropTypes.object,
    state: PropTypes.object,
    router: PropTypes.object
};

