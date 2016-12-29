/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, { Component, PropTypes } from 'react'
import GenKey from '../util/GenKey'

export default class Dropdown extends Component {
	constructor(props) {
		super(props);
		this.state = {
			id: GenKey(),
		}
	}
	componentDidMount() {
		document.body.addEventListener('click', () => {
			if(this.props.isOpen) {
				this.props.toggleOpen();
			}
		});
	}
	componentWillUnmount() {
		
	}
	renderInput(){
		if(this.props.inputPlaceholder || this.props.inputClassName) {
			return (
				<input className={this.props.inputClassName}
					   onClick={ () => this.props.toggleOpen() }
					   placeholder={this.props.inputPlaceholder} 
					   value={this.props.inputValue}
					   readOnly />
			);
		}
	}
	render(){
		let containerClassName = "DropdownContainer";
		let innerClassName = "Dropdown";

		if(this.props.isOpen) {
			innerClassName += ' Open';
		}

		return (
			<div className={containerClassName}>
				{this.renderInput()}
				<div className={innerClassName}>
					{this.props.listItems.map(this.props.renderItem)}
				</div>
			</div>
		);
	}
}

Dropdown.propTypes = {
	isOpen: PropTypes.bool.isRequired,
	toggleOpen: PropTypes.func.isRequired,
	listItems: PropTypes.array.isRequired,
	renderItem: PropTypes.func.isRequired,
	inputValue: PropTypes.string,
	inputPlaceholder: PropTypes.string,
	inputClassName: PropTypes.string,
};

Dropdown.contextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};