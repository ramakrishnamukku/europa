import React, {Component} from 'react'
import ContentRow from './../components/ContentRow'

export default class AddRegistry extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	componentDidMount() {
		this.context.actions.listRegistries();
	}
	renderSelectProvider(){
		return (
			<div className="FlexColumn">
				<label>
					Docker Registry Provider
				</label>
				<select className="BlueBorder FullWidth" onChange={(e) => this.context.actions.updateNewRegistryField('provider', e)}>
				   <option value="">Select Amazon Container Registry or Google Container Registry</option>
				   <option value="GCR">Google Container Registry</option>
				   <option value="ECR">Amazon Container Registry</option>
				   <option value="DOCKERHUB">DockerHub</option>
				   <option value="PRIVATE">Private Registry</option>
				</select>
			</div>
		);
	}
	renderRegistryCredentials() {
		return (
			<div className="FlexColumn">
				<label>
					Registry Credentials
				</label>
				<div className="FlexRow Row">
					<div className="Flex1 Column">
						<label className="small">
							Key Name
						</label>
						<input className="BlueBorder FullWidth" 
						       placeholder="Enter Key Name.."
							   onChange={(e) => this.context.actions.updateNewRegistryField('name', e)} />
					</div>
					<div className="Flex1">
						<label className="small">
							Key Region
						</label>
						<select className="BlueBorder FullWidth" onChange={(e) => this.context.actions.updateNewRegistryField('region', e)}>
						   <option value="">Select Region...</option>
						   <option value="us-west-1">us-west-1</option>
						   <option value="us-west-2">us-west-2</option>
						   <option value="us-east-1">us-east-1</option>
						   <option value="us-east-2">us-east-2</option>
						</select>
					</div>
				</div>
				<div className="FlexRow Row">
					<div className="Flex1 Column">
						<label className="small">
							Public Key
						</label>
						<input className="BlueBorder FullWidth" 
							   placeholder="Enter Public Key.."
							   onChange={(e) => this.context.actions.updateNewRegistryField('key', e)} />
					</div>
					<div className="Flex1">
						<label className="small">
							Private Key
						</label>
						<input className="BlueBorder FullWidth" 
							   placeholder="Enter Private Key.."
							   onChange={(e) => this.context.actions.updateNewRegistryField('secret', e)} />
					</div>
				</div>
			</div>
		);
	}
	renderAddRegistry(){
		let rows = [{
			columns: [{
                icon:'icon icon-dis-docker',
                renderBody: this.renderSelectProvider.bind(this)
            }]
		}, {
			columns: [{
                icon:'icon icon-dis-credential',
                renderBody: this.renderRegistryCredentials.bind(this)
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
	renderAddButton(){
		return (
			<div onClick={() => this.context.actions.addRegistryRequest()}>
				Add Registry
			</div>
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
				    {this.renderAddButton()}
				</div>
			</div>
		);
	}
}

AddRegistry.childContextTypes = {
    actions: React.PropTypes.object
};

AddRegistry.contextTypes = {
    actions: React.PropTypes.object
};