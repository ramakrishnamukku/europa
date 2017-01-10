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
			activeRecordUrls: []
		};
	}
	toggleActiveRecord(url){
		if(this.state.activeRecordUrls.includes(url)) {
			this.setState({
				activeRecordUrls: this.state.activeRecordUrls
					.filter((i) => {
						return !(i == url)
					})
			});
		} else {
			this.setState({
				activeRecordUrls: [...this.state.activeRecordUrls, url]
			});			
		}
	}
	isRecordActive(url){
		return this.state.activeRecordUrls.includes(url);
	}
	renderExpandIcon(record){
		console.log(record);
		console.log(this.state.activeRecordUrls);
		console.log(this.isRecordActive(record.notififcationId));
		return (this.isRecordActive(record.notififcationId)) ? ( <i className="icon icon-dis-collapse" /> ) 
												 			 : ( <i className="icon icon-dis-expand" /> )
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
				{this.props.allWebhookData.map((record, index) => {
					return (
						<div key={index} className="RecordListItem">
							<div className="RecordListItemInfo" onClick={() => this.toggleActiveRecord(record.notificationId)}>
								{this.renderStatusIcon(record)}
								<div className="Url">{record.url}</div>
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
				<WebhookData webhookData={record} close={() => this.toggleActiveRecord(record.notificationId)}/>
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
    state: PropTypes.object,
    router: PropTypes.object
};

WebhookViewer.contextTypes = {
    actions: PropTypes.object,
    state: PropTypes.object,
    router: PropTypes.object
};
