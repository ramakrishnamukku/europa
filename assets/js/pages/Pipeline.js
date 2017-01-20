import React, {Component, PropTypes} from 'react'
import { Link } from 'react-router'
import Btn from './../components/Btn'
import Loader from './../components/Loader'
import BtnGroup from './../components/BtnGroup'
import ControlRoom from './../components/ControlRoom'
import CenteredConfirm from './../components/CenteredConfirm'
import PipelineStageItem from './../components/PipelineStageItem'
import PipelineConnectRepository from './../components/PipelineConnectRepository'

export default class Pipeline extends Component {
  constructor(props) {
    super(props);
    this.state = {
      pipelineContainerRepo: null,
      loading: true
    };
  }
  componentDidMount() {
    this.context.actions.listRepos()
    .then( () => {
      this.context.actions.getPipeline(this.props.params.pipelineId)
      .then(pipeline => {
        let repo = this.props.repos.filter(repo => repo.id == pipeline.containerRepoId)[0];
        this.setState({
          pipelineContainerRepo: repo || null,
          loading: false
        })
      });
    })
  }
  renderPage(pipeline) {
    switch (this.props.pipelineStore.section) {
      case "CONNECT_REPOSITORY":
        return this.renderConnectRepo();
      break;

      case "ADD_STAGE":
        return this.renderAddStage();
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
    return (
      <div>
        the pipeline
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
        onClick: this.context.actions.toggleInitNewPipeline,
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
