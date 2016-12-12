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
	renderRepoEventItem(event, index){
		return (
			<RepoEventItem key={index}
						   event={event} />
		);
	}
	renderAllEvents(){
		return (
			<div className="Timeline">
				<div className="FlexRow SpaceBetween">
					<div className="FlexRow">
						<label>
							Event History
						</label>
					</div>
				</div>
				<div className="TimelineContainer">
					{this.props.events.map(this.renderRepoEventItem)}
				</div>
			</div>
		);
	}
	renderTimeline(){
		let rows = [{
			columns: [{
                icon:'icon icon-dis-webhook-circle-solid',
                renderBody: this.renderAllEvents.bind(this)
            }]
		}];

		return rows.map(this.renderContentRow);
	}
	renderContentRow(row, index){
		return (
			<ContentRow key={index}
						row={row} />
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

