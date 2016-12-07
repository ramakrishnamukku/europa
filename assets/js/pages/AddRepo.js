import React, {Component} from 'react'
import AddRegistry from './../components/AddRegistry'
import ContentRow from './../components/ContentRow'
import Btn from './../components/Btn'

let dockerRepoKey = 'dockerImage'
let webhookKey = "webhookUrl";
let secretKey = "secret"

export default class AddRepository extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	inputClassName(selector){
		let hasSelector = this.context.state.addRepo.errorFields.includes(selector)
		if(hasSelector) {
			return "BlueBorder FullWidth Error";
		} else {
		    return "BlueBorder FullWidth";
		}
	}		
	renderAddRegistry(){
		return (
			<AddRegistry 
				standaloneMode={false}
			/>
		);
	}
	renderDockerRepository(){
		return (
			<div className="FlexColumn">
				<label>
					Docker Image Repository
				</label>
				<input className={this.inputClassName(dockerRepoKey)} 
				       placeholder="Enter Docker Repository.."
				       value={this.context.state.addRepo.newRepo[dockerRepoKey]}
					   onChange={(e) => this.context.actions.updateNewRepoField(dockerRepoKey, e)} />
			</div>
		);
	}
	renderWebhook(){
		return (
			<div className="FlexColumn">
				<div className="Row">
					<label>
						Docker Image Repository
					</label>
					<input className={this.inputClassName(webhookKey)} 
					       value={this.context.state.addRepo.newRepo[webhookKey]}
						   placeholder="Enter Webhook URL.."
					       onChange={(e) => this.context.actions.updateNewRepoField(webhookKey, e)} />
				</div>
				<div className="Row">
					<label>
						Secret (optional)
					</label>
					<input className={this.inputClassName(secretKey)} 
					       value={this.context.state.addRepo.newRepo[secretKey]}
						   placeholder="Enter Secret"
					       onChange={(e) => this.context.actions.updateNewRepoField(secretKey, e)} />
				</div>
			</div>
		);
	}
	renderTestWebhookButton(){
		return (
			<Btn onClick={() => this.context.actions.testWebhookUrl()}
				 text="Test Webhook"
				 canClick={!!this.context.state.addRepo.newRepo[webhookKey]}
				 help="Clicking this button will send a payload to the specified URL."/>
		);
	}
	renderAddRepoButton(){
		let canAdd = (this.context.actions.canAddRegistry() && this.context.actions.canAddRepo());
		return (
			<Btn onClick={() => this.addRepo()}
				 text="Add Repository"
				 canClick={canAdd}
				 help="Clicking this button will send a payload to the specified URL."/>
		);
	}
	addRepo(){
		this.context.actions.addRegistryRequest()
			.then(this.context.actions.addRepoRequest)
			.catch(() => console.error('Add Registry Errors -- Skipping add repo'))
	}
	renderAddRepository(){
		let rows = [{
			columns: [{
                icon:'icon icon-dis-container',
                renderBody: this.renderDockerRepository.bind(this)
            }]
		}, {
			columns: [{
                icon:'icon icon-dis-webhook',
                renderBody: this.renderWebhook.bind(this)
            }]
		}, {
			columns: [{
                icon:'icon icon-dis-blank',
                renderBody: this.renderTestWebhookButton.bind(this)
            }]
		}, {
			columns: [{
                icon:'icon icon-dis-blank',
                renderBody: this.renderAddRepoButton.bind(this)
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
			<div className="ContentContainer">
				<h2 className="PageHeader">
					Let's get started...
				</h2>
				<div>
				    {this.renderAddRegistry()}
					{this.renderAddRepository()}
				</div>
			</div>
		);
	}
}

AddRepository.childContextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object
};

AddRepository.contextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object
};
