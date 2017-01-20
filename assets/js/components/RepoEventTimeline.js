/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import NPECheck from './../util/NPECheck'
import RepoEventItem from './../components/RepoEventItem'

export default class RepoEventTimeline extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	renderLegend(){
		return (
			<div className="TimelineLegend">
				Event Timeline
				{this.renderLoadingIcon()}
			</div>
		);
	}
	renderLoadingIcon(){
		if(!NPECheck(this.props, 'events/length', true)) {
			return (
				<i className="icon icon-dis-waiting rotating"/>
			);
		}
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
		if(!NPECheck(this.props, 'events/length', true)) {
			return (
				<div className="Timeline">
					<div className="NoContent">
						<h3>
							No Events Found
						</h3>
						<p> If you just added this repository, it may take a second to populate historical events.</p>
					</div>
				</div>
			);
		}

		return this.props.events.sort((firstEvent, secondEvent) => (firstEvent.eventTime >= secondEvent.eventTime) ? -1 : 1 )
								.map((event, index) => this.renderRepoEventItem(event, index))
	}
	renderRepoEventItem(event, index){
		return (
			<RepoEventItem {...this.props}
						   key={index}
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
	events: PropTypes.array.isRequired
};

RepoEventTimeline.childContextTypes = {
    actions: PropTypes.object
};

RepoEventTimeline.contextTypes = {
    actions: PropTypes.object
};

