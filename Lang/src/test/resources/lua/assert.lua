local assert = {}

function failure(actual, expected, message)
    return {
        ['actual'] = actual,
        ['expected'] = expected,
        ['message'] = message
    }
end

function assert.equal(actual, expected)
    if actual ~= expected then
        return failure(actual, expected, 'assert.equal() failed: ' .. tostring(actual) .. ' != ' .. tostring(expected))
    end
end

function assert.notEqual(actual, expected)
    if actual == expected then
        return failure(actual, expected, 'assert.notEqual() failed: ' .. tostring(actual) .. ' == ' .. tostring(expected))
    end
end

function assert.greater(actual, expected)
    if actual <= expected then
        return failure(actual, expected, 'assert.greater() failed: ' .. tostring(actual) .. ' <= ' .. tostring(expected))
    end
end

function assert.lesser(actual, expected)
    if actual >= expected then
        return failure(actual, expected, 'assert.lesser() failed: ' .. tostring(actual) .. ' >= ' .. tostring(expected))
    end
end

function assert.greaterOrEqual(actual, expected)
    if actual < expected then
        return failure(actual, expected, 'assert.greaterOrEqual() failed: ' .. tostring(actual) .. ' < ' .. tostring(expected))
    end
end

function assert.lesserOrEqual(actual, expected)
    if actual > expected then
        return failure(actual, expected, 'assert.lesserOrEqual() failed: ' .. tostring(actual) .. ' > ' .. tostring(expected))
    end
end

return assert