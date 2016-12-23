/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import WebhookData from './../components/WebhookData'
import TimelineIcons from './../util/TimelineIcons'

export default class RepoEventItem extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	renderEventData(event){
		if(this.context.state.repoDetails.activeEventId == event.id) {
			return (
				<WebhookData webhookData={{}}/>
			);
		} 
	}
	render() {
		let event = this.props.event;

		return (
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

