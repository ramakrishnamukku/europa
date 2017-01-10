/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, { Component, PropTypes } from 'react'

export default class BtnGroup extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	renderButton(b, i){
		let btnClass = 'IconBtn';

		if(b.isActive) {
			btnClass += ' Active';
		}

		return (
			<div key={i} className={btnClass} 
				 onClick={ b.onClick ? b.onClick : null }
				 style={(b.leftMargin) ? {marginLeft: '5px'} : {}} 
				 data-tip={b.toolTip} 
				 data-for="ToolTipBottom">
				<i className={b.icon} />
			</div>
		);
	}
	render(){
		return (
			<div className="BtnGroup">
				{this.props.buttons.map(this.renderButton)}
			</div>
		);
	}
}

BtnGroup.propTypes = {
	buttons: PropTypes.arrayOf(
	  PropTypes.shape({
	    icon: PropTypes.string,
	    onClick: PropTypes.func,
		isActive: PropTypes.bool,
		leftMargin: PropTypes.bool,
		toolTip: PropTypes.string
	  })
	)
}
