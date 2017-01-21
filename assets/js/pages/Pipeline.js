import React, {Component, PropTypes} from 'react'
import { Link } from 'react-router'
import Btn from './../components/Btn'
import Loader from './../components/Loader'
import BtnGroup from './../components/BtnGroup'
import ControlRoom from './../components/ControlRoom'
import CenteredConfirm from './../components/CenteredConfirm'
import PipelineStageItem from './../components/PipelineStageItem'
import PipelineConnectRepository from './../components/PipelineConnectRepository'
import * as GR from './../reducers/GeneralReducers'

export default class Pipeline extends Component {
  constructor(props) {
    super(props);
    this.state = {
      repoMapById: {},
      loading: true
    };
  }
  componentDidMount() {
    this.context.actions.listRepos()
    .then( () => {
      this.context.actions.getPipeline(this.props.params.pipelineId)
      .then(pipeline => {
        this.setState({
          loading: false,
          repoMapById: this.props.repos.reduce((map, repo) => {
            map[repo.id] = repo;
            return map;
          }, {} )
        })
      });
    })
  }
  componentWillUnmount() {
    this.context.actions.resetSinglePipelineState();
  }
  renderPage(pipeline) {
    switch (this.props.pipelineStore.section) {
      case "CONNECT_REPOSITORY":
        return this.renderConnectRepo();
      break;

      case "ADD_STAGE":
        return this.renderConnectStage();
      break;

      case "REMOVE_STAGE":
        return this.renderRemoveStage();
      break;

      default:
        if (!pipeline.containerRepoId) {
          return (
            <div>
              <PipelineStageItem {...this.props} empty={true} />
              <div className="FlexRow JustifyCenter AlignCenter">
                <Btn onClick={() => this.context.actions.setPipelinePageSection("CONNECT_REPOSITORY") }
                     className="LargeBlueButton"
                     text="Connect Repository"
                     style={{marginTop: '28px'}}
                     canClick={true} />
              </div>
            </div>
          );
        } else {
          return this.renderPipeline();
        }
    }
  }
  renderPipeline() {
    if (!this.state.repoMapById) return;
    let repoContainerPipeline = this.state.repoMapById[this.props.pipelineStore.pipeline.containerRepoId]

    return (
      <div>
        <PipelineStageItem {...this.props}
                           firstStage={true}
                           repo={repoContainerPipeline} />
        {this.props.pipelineStore.pipeline.components.map((component, idx) => {
          return (
            <PipelineStageItem {...this.props}
                               key={component.id}
                               idx={idx}
                               pipelineComponent={component}
                               repo={this.state.repoMapById[component.destinationContainerRepoId]} />
          );
        })}
        <div className="FlexRow JustifyCenter AlignCenter">
          <Btn onClick={() => this.context.actions.setPipelinePageSection("ADD_STAGE") }
               className="LargeBlueButton"
               text="Add Stage"
               style={{marginTop: '28px'}}
               canClick={true} />
        </div>
      </div>
    );
  }
  // Connect Repo
  renderConnectRepo() {
    return (
      <div style={ {margin: "14px 0 0"} }>
        <ControlRoom renderBodyContent={ this.connectRepoForm.bind(this) } />
      </div>
    );
  }
  connectRepoForm() {
    return (
      <PipelineConnectRepository initialConnect={true}
                                 {...this.props} />
    );
  }
  // Connect Stage
  renderConnectStage() {
    return (
      <div style={ {margin: "14px 0 0"} }>
        <ControlRoom renderBodyContent={ this.connectStageForm.bind(this) } />
      </div>
    );
  }
  connectStageForm() {
    return (
      <PipelineConnectRepository {...this.props} />
    );
  }
  // Remove Stage
  renderRemoveStage() {
    return (
      <div style={ {margin: "14px 0 0"} }>
        <ControlRoom renderBodyContent={ this.removeStageForm.bind(this) } />
      </div>
    );
  }
  removeStageForm() {
    return (
      <div>
        <div className="CR_Header">
          <span className="CR_HeaderTitle">
            Remove Pipeline
          </span>
          <span className="CR_HeaderClose">
            <i className="icon-dis-close"
               onClick={ () => this.context.actions.setPipelinePageSection(null) } />
          </span>
        </div>
        <div className="CR_BodyContent">
          <div className="Flex1">
            <CenteredConfirm confirmButtonText="Remove"
                             message="Are you sure you want to remove this Pipeline?"
                             confirmButtonStyle={{}}
                             onConfirm={ this.context.actions.removePipeline }
                             onCancel={ () => this.context.actions.setPipelinePageSection(null) } />
          </div>
        </div>
      </div>
    );
  }
  render() {
    let pipeline = this.props.pipelineStore.pipeline;

    if (this.state.loading) {
      return (
        <div className="PageLoader">
          <Loader />
        </div>
      );
    }

    const buttons = [
      {
        icon: 'icon icon-dis-trash',
        toolTip: 'Remove Pipeline',
        onClick: () => this.context.actions.setPipelinePageSection("REMOVE_STAGE"),
        isActive: this.props.pipelinesStore.initNewPipeline
      }
    ]

    return (
      <div className="ContentContainer">
        <div className="PageHeader">
          <h2>
             {pipeline.name}
          </h2>
          <div className="FlexRow">
            <div className="Flex1">
              <BtnGroup buttons={buttons} />
            </div>
          </div>
        </div>
        <div className="project-pipeline">
          <div className="stages">
            {this.renderPage(pipeline)}
          </div>
        </div>
      </div>
    );
  }
}

Pipeline.childContextTypes = {
    actions: PropTypes.object,
    router: PropTypes.object
};

Pipeline.contextTypes = {
    actions: PropTypes.object,
    router: PropTypes.object
};
