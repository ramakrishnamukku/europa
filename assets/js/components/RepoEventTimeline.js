/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import WebhookData from './../components/WebhookData'
import RepoEventItem from './../components/RepoEventItem'
import ContentRow from './../components/ContentRow'

export default class RepoEventTimeline extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	renderWebhookData(){
		return (
			<div>
				Webhook Data
			</div>
		);
	}
	renderAllEvents(){
		return (
			<div className="Timeline">
				<div className="TimelineContainer">
					{this.props.events.map(this.renderRepoEventItem)}
				</div>
			</div>
		);
	}
	renderRepoEventItem(event, index){
		return (
			<RepoEventItem key={index}
						   event={event} />
		);
	}
	render() {
		return (
			<div className="RepoEventTimeline">
				{this.renderAllEvents()}
			</div>
		);
	}	
}


RepoEventTimeline.propTypes = {
	events: React.PropTypes.array.isRequired
};

RepoEventTimeline.childContextTypes = {
    actions: PropTypes.object,
    state: PropTypes.object,
    router: PropTypes.object
};

RepoEventTimeline.contextTypes = {
    actions: PropTypes.object,
    state: PropTypes.object,
    router: PropTypes.object
};

