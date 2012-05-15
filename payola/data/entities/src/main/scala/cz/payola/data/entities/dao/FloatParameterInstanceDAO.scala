package cz.payola.data.entities.dao

import cz.payola.data.entities.PayolaDB
import cz.payola.data.entities.analyses.parameters.FloatParameterValue

class FloatParameterInstanceDAO extends EntityDAO[FloatParameterValue](PayolaDB.floatParameterValues)
{
}