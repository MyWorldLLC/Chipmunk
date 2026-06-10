local assert = {}

function result(actual, expected, condition, message)
    return {
        ['actual'] = actual,
        ['expected'] = expected,
        ['failed'] = not condition,
        ['message'] = message
    }
end

function assert.equal(actual, expected)
    return result(actual, expected, actual == expected, 'assert.equal() failed: ' .. tostring(actual) .. ' != ' .. tostring(expected))
end

function assert.notEqual(actual, expected)
    return result(actual, expected, actual ~= expected, 'assert.notEqual() failed: ' .. tostring(actual) .. ' == ' .. tostring(expected))
end

function assert.greater(actual, expected)
    return result(actual, expected, actual > expected, 'assert.greater() failed: ' .. tostring(actual) .. ' <= ' .. tostring(expected))
end

function assert.lesser(actual, expected)
    return result(actual, expected, actual < expected, 'assert.lesser() failed: ' .. tostring(actual) .. ' >= ' .. tostring(expected))
end

function assert.greaterOrEqual(actual, expected)
    return result(actual, expected, actual >= expected, 'assert.greaterOrEqual() failed: ' .. tostring(actual) .. ' < ' .. tostring(expected))
end

function assert.lesserOrEqual(actual, expected)
    return result(actual, expected, actual <= expected, 'assert.lesserOrEqual() failed: ' .. tostring(actual) .. ' > ' .. tostring(expected))
end

return assert