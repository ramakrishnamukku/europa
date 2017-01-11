/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import WebhookData from './../components/WebhookData'
import StatusCode from './../util/StatusCode'

export default class WebhookViewer extends Component {
	constructor(props) {
		super(props);
		this.state = {
			activeRecordIds: []
		};
	}
	toggleActiveRecord(notificationId){
		if(this.isRecordActive(notificationId)) {
			this.setState({
				activeRecordIds: this.state.activeRecordIds
					.filter((i) => {
						return !(i == notificationId)
					})
			});
		} else {
			this.setState({
				activeRecordIds: [...this.state.activeRecordIds, notificationId]
			});			
		}
	}
	isRecordActive(notificationId){
		return this.state.activeRecordIds.includes(notificationId);
	}
	renderExpandIcon(record){
		return (this.isRecordActive(record.notificationId)) ? ( <i className="icon icon-dis-collapse" /> ) 
												 			: ( <i className="icon icon-dis-expand" /> );
	}
	renderStatusIcon(record){
		let status = StatusCode(record.response.httpStatusCode);
		let className;

		switch(status) {
			case 'SUCCESS': 
				className = 'icon icon-dis-check';
			break;
			case 'ERROR': 
				className = 'icon icon-dis-alert';
			break;
			case 'WARNING': 
				className = 'icon icon-dis-alert Warning';
			break;
		}

		return (
			<i className={className} />
		);
	}
	renderRecordList(){
		return (
			<div className="RecordList">
				{this.props.allWebhookData.sort((firstEvent, secondEvent) => (firstEvent.notificationTime > secondEvent.notificationTime) ? -1 : 1 ).map((record, index) => {
					return (
						<div key={index} className="RecordListItem">
							<div className="RecordListItemInfo" onClick={() => this.toggleActiveRecord(record.notificationId)}>
								{this.renderStatusIcon(record)}
								<div className="Url" data-tip={record.url} data-for="ToolTipTop">{record.url}</div>
								<div className="Id">{record.notificationId}</div>
								<div className="Id">{record.notificationTime}</div>
								{this.renderExpandIcon(record)}
							</div>
							{this.renderWebhookData(record)}
						</div>
					);
				})}
			</div>
		);
	}
	renderWebhookData(record){
		if(this.isRecordActive(record.notificationId)) {
			return (
				<WebhookData {...this.props} webhookData={record} close={() => this.toggleActiveRecord(record.notificationId)}/>
			);		
		}

	}
	render() {	
		return (
			<div className="WebhookViewer">
				<div className="Delivered">Delivered Webhooks</div>
				{this.renderRecordList()}
			</div>
		);		
	}
}

WebhookViewer.propTypes = {
	allWebhookData: PropTypes.array.isRequired
};

WebhookViewer.childContextTypes = {
    actions: PropTypes.object,
};

WebhookViewer.contextTypes = {
    actions: PropTypes.object,
};
