import React, {Component, PropTypes} from 'react'
import WebhookData from './../components/WebhookData'

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
		} else {
			return (
				<div className="EventLine">

				</div>
			)
		}
	}
	render() {
		
		let event = this.props.event;

		return (
			<div className="RepoEventItem">
				<div className="EventDetails">
					<div className="Flex2">
						<i className="icon icon-dis-push" />
						Pushed image&nbsp;
						<span className="LightBlueColor">{event.image}</span>
						 &nbsp;with tag&nbsp;
						 <span className="Tag"> {event.tag}</span>
					</div>
					<div className="Flex1">
						 <span className="EventTime Flex1">
						 	6 Days Ago
						 </span>
					</div>
					<div className="Flex1 EventStatus">
						 <span className="Flex1">
						 	Status: &nbsp; <span className="Status">Success (200)</span>
						 </span>
					</div>
					<div className="Actions">
						<span className="Pipe">|</span>
						<i className="icon icon-dis-details"
						   onClick={() => this.context.actions.toggleEventDetails(event.id)}/> 
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

