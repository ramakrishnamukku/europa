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
	renderLegend(){
		return (
			<div className="TimelineLegend">
				Event Timeline
			</div>
		);
	}
	renderTimeline(){
		return (
			<div className="Timeline">
				{this.renderLegend()}
				<div className="TimelineContainer">
					{this.renderTimelineContent()}
				</div>	
			</div>
		);
	}
	renderTimelineContent(){
		if(!this.props.events || !this.props.events.length) {
			return (
				<div className="Timeline">
					<div className="NoContent">
						<h3>
							No Events Found
						</h3>
						<p> If you just added this repoistory, it may take a second to populate historical events.</p>
					</div>
				</div>
			);
		}


		return this.props.events.sort((firstEvent, secondEvent) => (firstEvent.eventTime >= secondEvent.eventTime) ? -1 : 1 )
								.map(this.renderRepoEventItem)
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
				{this.renderTimeline()}
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

