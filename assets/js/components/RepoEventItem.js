/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import WebhookViewer from './../components/WebhookViewer'
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

			return (
				<WebhookViewer allWebhookData={this.context.state.notif.currentNotifRecords} />
			);
		} 
	}
	renderWebhookText(event){
		let notifLength = event.notifications.length;
		let verb = (this.context.state.repoDetails.activeEventId == event.id) ? 'Hide' : 'View';
		return (notifLength > 1) ? `${verb} Webhooks (${notifLength})` : `${verb} Webhook (${notifLength})`;
	}
	render() {
		let event = this.props.event;
		let time = event.eventTime;
		let friendlyTime = ConvertTimeFriendly(time);
		let timeUTC = ConvertTimeUTC(new Date(time), true);
		let SHA = event.imageSha;

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
							<span className="Item" onClick={ () => this.viewNotificationInfo(event) }>{this.renderWebhookText(event)}</span>
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

