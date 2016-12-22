import React, {Component} from 'react'
import { Link } from 'react-router'
import ReactTooltip from 'react-tooltip'
import Btn from './../components/Btn'
import ContentRow from './../components/ContentRow'
import RegistryProviderIcons from './../util/RegistryProviderIcons'
import CenteredConfirm from './../components/CenteredConfirm'
import Msg from './../components/Msg'
import Loader from './../components/Loader'

export default class Registries extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	componentDidMount() {
		this.context.actions.listRegistries();
	}
	componentWillUnmount() {
		this.context.actions.resetRegistryState();	
	}
	renderRegistries(){
		if(this.context.state.registriesXHR) {
			return (
				<div className="PageLoader">
					<Loader />
				</div>	
			);
		}

		let registries = this.context.state.registries;

		if(!registries.length) {
			return this.renderNoRegistries()
		}

		return (
			<div className="RegistryList FlexColumn">
				{this.renderLegend()}
				{registries.map(this.renderRegistryItem.bind(this))}
			</div>
		);
	}
	renderLegend(){
		return (
			<div className="RegistryLegend">
				<span>Provider</span>
				<span>Key Name</span>
				<span>Access Key</span>
				<span>Region</span>
				<span>+ Add Credential</span>
			</div>
		);
	}
	renderRegistryItem(reg, index){
		return (
			<div key={index}
				 className="Flex1 RegistryItem FlexColumn">
				<div className="Inside FlexRow">
					
					<span className="ListValue">
						<img className="ProviderIcon"src={RegistryProviderIcons(reg.provider)}/>
						{reg.provider}
					</span>
					<span className="ListValue">{reg.name}</span>
					<span className="ListValue">{reg.name}</span>
					<span className="ListValue">{reg.region}</span>
					<span className="Actions">
						<i className="icon icon-dis-edit" data-tip="Edit Credentials" data-for="ToolTipTop"
						   onClick={() => this.setRegistryForEdit(reg)}/>
						<i className="icon icon-dis-trash" data-tip="Delete Credentials" data-for="ToolTipTop" 
							onClick={() => this.context.actions.setRegistryForDelete(reg)}
						/>
					</span>
				</div>
				{this.renderConfirmDeleteRegistry(reg)}
			</div>
		);
	}
	setRegistryForEdit(reg){
		this.context.actions.setRegistryForEdit(reg)
		.then(() => this.context.router.push('/edit-registry'))
	}
	renderNoRegistries(){
		return (
			<div className="NoContent">
				<h3>
					No Registry Credentials Saved
				</h3>		
			</div>
		);
	}
	renderConfirmDeleteRegistry(reg){
		if(reg == this.context.state.registry.registrySelectedForDelete) {

			if(this.context.state.registry.deleteRegistryErrorMsg) {
				return (
					<div className="RowPadding">
						<Msg text={this.context.state.registry.deleteRegistryErrorMsg} 
								  close={() => this.context.actions.setRegistryForDelete()}/>
					</div>
				)
			}

			if(this.context.state.registry.deleteRegistryXHR) {
				return (
					<Loader />
				);
			}
			
			return (
				<CenteredConfirm message="Are you sure you want to delete this registry?"
							     confirmButtonText="Delete"
							     confirmButtonStyle={{}}
							     onConfirm={() => this.context.actions.deleteRegistry()}
							     onCancel={() => this.context.actions.setRegistryForDelete(null)}/>
			);	
		}
	}
	render() {
		return this.renderRegistries()
	}
}

Registries.childContextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};

Registries.contextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};